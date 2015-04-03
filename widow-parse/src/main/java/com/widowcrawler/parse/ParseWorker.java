package com.widowcrawler.parse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.IndexInput;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.model.ParseInput;
import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.worker.Worker;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Scott Mansfield
 */
public class ParseWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(ParseWorker.class);

    // TODO: This really needs to be configuration
    private static final String INDEX_QUEUE = "widow-index";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    QueueManager queueManager;

    private ParseInput parseInput;

    public ParseWorker withInput(ParseInput input) {
        this.parseInput = input;
        return this;
    }

    // TODO: Major: this should be pluggable for different paths / formats / etc.
    // the current implementation will be a default

    @Override
    public boolean doWork() {

        try {
            Document document = Jsoup.parse(parseInput.getPageContent());

            IndexInput.Builder builder = new IndexInput.Builder()
                    .withExistingAttributes(parseInput.getAttributes());

            int pageContentSize = parseInput.getPageContent().length();
            builder.withAttribute(PageAttribute.CONTENT_SIZE, pageContentSize);

            // Record title, even if it's just whitespace
            String title = document.title();
            if (title != null) {
                builder.withAttribute(PageAttribute.TITLE, title);
            }

            // get links
            final Elements aTagElements = document.getElementsByTag("a");
            final List<String> outLinks = new LinkedList<>();

            for (Element element : aTagElements) {
                String href = element.attr("href");

                if (StringUtils.isNotBlank(href)) {
                    outLinks.add(href);
                }
            }

            builder.withAttribute(PageAttribute.OUT_LINKS, outLinks);

            // get asset links
            // link tags (href)
            final Elements linkTagElements = document.getElementsByTag("link");
            final List<String> cssLinks = new LinkedList<>();

            for (Element element : linkTagElements) {
                String href = element.attr("href");

                if (StringUtils.isNotBlank(href)) {
                    cssLinks.add(href);
                }
            }

            builder.withAttribute(PageAttribute.CSS_LINKS, cssLinks);

            // script tags (src)
            final Elements scriptTagElements = document.getElementsByTag("script");
            final List<String> jsLinks = new LinkedList<>();

            for (Element element : scriptTagElements) {
                String src = element.attr("src");

                if (StringUtils.isNotBlank(src)) {
                    jsLinks.add(src);
                }
            }

            builder.withAttribute(PageAttribute.JS_LINKS, jsLinks);

            // img tags (src)
            final Elements imgTagElements = document.getElementsByTag("img");
            final List<String> imgLinks = new LinkedList<>();

            for (Element element : imgTagElements) {
                String src = element.attr("src");

                if (StringUtils.isNotBlank(src)) {
                    imgLinks.add(src);
                }
            }

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

                // TODO: normalize links!!!
                final Response response = ClientBuilder.newClient().target(link).request().buildGet().invoke();

                // read the response into a String to guarantee we get a length
                // rather than rely on a server returning a Content-Length header
                int assetSize = response.readEntity(String.class).length();

                totalPageSize += assetSize;
            }

            builder.withAttribute(PageAttribute.SIZE_WITH_ASSETS, totalPageSize);

            queueManager.enqueue(INDEX_QUEUE, objectMapper.writeValueAsString(builder.build()));

            return true;

        } catch (Exception ex) {
            logger.error("Parsing failed", ex);
            return false;
        }
    }
}
