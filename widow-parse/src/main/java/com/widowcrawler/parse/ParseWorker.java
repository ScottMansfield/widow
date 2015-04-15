package com.widowcrawler.parse;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.FetchInput;
import com.widowcrawler.core.model.IndexInput;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.model.ParseInput;
import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.worker.Worker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static com.widowcrawler.core.retry.Retry.retry;

/**
 * @author Scott Mansfield
 */
public class ParseWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(ParseWorker.class);

    private static final Set<String> sentToFetch = new HashSet<>();
    private static final Map<String, Integer> cachedAssetSizes = new HashMap<>();

    // TODO: This really needs to be configuration
    private static final String FETCH_QUEUE = "widow-fetch";
    private static final String INDEX_QUEUE = "widow-index";
    private static final String BUCKET_NAME = "widow-test";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    QueueManager queueManager;

    @Inject
    LinkNormalizer linkNormalizer;

    @Inject
    AmazonS3 amazonS3Client;

    private ParseInput parseInput;

    public ParseWorker withInput(ParseInput input) {
        this.parseInput = input;
        return this;
    }

    // TODO: Major: this should be pluggable for different paths / formats / etc.
    // think img/jpeg should not be parsed
    // feeds can be parsed in a different way (i.e. RSS XML) to pull out links
    // application/atom+xml
    //
    // the current implementation will be a default

    @Override
    public boolean doWork() {

        try {
            // get page content from S3
            String pageContentRef = parseInput.getAttribute(PageAttribute.PAGE_CONTENT_REF).toString();
            GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, pageContentRef);

            final S3Object s3Object = retry(() -> amazonS3Client.getObject(getObjectRequest));

            final String pageContent = IOUtils.toString(s3Object.getObjectContent());
            IOUtils.closeQuietly(s3Object.getObjectContent());

            Document document = Jsoup.parse(pageContent);

            IndexInput.Builder builder = new IndexInput.Builder().withExistingAttributes(parseInput.getAttributes());

            int pageContentSize = pageContent.length();
            builder.withAttribute(PageAttribute.CONTENT_SIZE, pageContentSize);

            // Record title, even if it's blank
            String title = document.title();
            if (title != null) {
                builder.withAttribute(PageAttribute.TITLE, title);
            }

            // get links
            final Set<String> outLinks = collectLinks(document, "a", "href");
            builder.withAttribute(PageAttribute.OUT_LINKS, outLinks);

            // get asset links
            // link tags (href)
            final Set<String> cssLinks = collectLinks(document, "link", "href");
            builder.withAttribute(PageAttribute.CSS_LINKS, cssLinks);

            // script tags (src)
            final Set<String> jsLinks = collectLinks(document, "script", "src");
            builder.withAttribute(PageAttribute.JS_LINKS, jsLinks);

            // img tags (src)
            final Set<String> imgLinks = collectLinks(document, "img", "src");
            builder.withAttribute(PageAttribute.IMG_LINKS, imgLinks);

            // retrieve all assets and calculate total page size
            int totalPageSize = pageContentSize;
            List<String> pageLinks = new ArrayList<>(cssLinks.size() + jsLinks.size() + imgLinks.size());
            pageLinks.addAll(cssLinks);
            pageLinks.addAll(jsLinks);
            pageLinks.addAll(imgLinks);

            for (String link : pageLinks) {
                // TODO: Metrics on all asset load times as well
                // it's hard to tell what's necessary to render above the fold, but we can add it all together....

                String norm = linkNormalizer.normalize(parseInput.getAttribute(PageAttribute.ORIGINAL_URL).toString(), link);
                Integer assetSize = cachedAssetSizes.get(norm);

                if (assetSize == null) {
                    logger.info("Normalized URI. Original: " + link + " | Normalized: " + norm);

                    final Response response = ClientBuilder.newClient().target(norm).request().buildGet().invoke();

                    // read the response into a String to guarantee we get a length
                    // rather than rely on a server returning a Content-Length header
                    assetSize = response.readEntity(String.class).length();

                    cachedAssetSizes.put(norm, assetSize);

                    // TODO: Parse CSS and pull in any referenced images and external css
                }

                totalPageSize += assetSize;
            }

            builder.withAttribute(PageAttribute.SIZE_WITH_ASSETS, totalPageSize);

            // TODO: insert whatever custom parsing here

            queueManager.enqueue(INDEX_QUEUE, objectMapper.writeValueAsString(builder.build()));

            outLinks.stream()
                    .map((link) -> linkNormalizer.normalize(parseInput.getAttribute(PageAttribute.ORIGINAL_URL).toString(), link))
                    .filter(StringUtils::isNotBlank)
                    .forEach((link) -> {
                        if (sentToFetch.contains(link)) return;

                        try {
                            logger.info("Enqueuing fetch message for " + link);
                            FetchInput fetchInput = new FetchInput(link);
                            queueManager.enqueue(FETCH_QUEUE, objectMapper.writeValueAsString(fetchInput));
                            sentToFetch.add(link);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex.getMessage(), ex);
                        }
                    });

            return true;

        } catch (Exception ex) {
            logger.error("Parsing failed", ex);
            return false;
        }
    }

    private Set<String> collectLinks(Document document, String tagName, String attrName) {
        final Elements elements = document.getElementsByTag(tagName);
        return elements.stream()
                .map((elem) -> elem.attr(attrName))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}
