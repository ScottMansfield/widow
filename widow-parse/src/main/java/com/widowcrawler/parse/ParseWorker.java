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
import com.widowcrawler.core.util.DomainUtils;
import com.widowcrawler.core.worker.Worker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

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

    private static final String FETCH_QUEUE_NAME_CONFIG_KEY = "com.widowcrawler.queue.fetch";
    private static final String NEXT_QUEUE_CONFIG_KEY = "com.widowcrawler.queue.next";
    private static final String BUCKET_NAME_CONFIG_KEY = "com.widowcrawler.bucket.name";
    private static final String USE_REMOTE_CACHE_CONFIG_KEY = "com.widowcrawler.parse.use.remote.cache";
    private static final String USE_BASE_DOMAIN_CONFIG_KEY = "com.widowcrawler.use.base.domain";
    private static final String BASE_DOMAIN_CONFIG_KEY = "com.widowcrawler.base.domain";

    private static final String SENT_TO_FETCH_KEY_PREFIX = "sentToFetch:";
    private static final String ASSET_SIZE_KEY_PREFIX = "assetSize:";

    private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";
    private static final String NO_CONTENT_TYPE_KEY = "NO_CONTENT_TYPE";
    private static final String EXCEPTION_KEY = "EXCEPTION";

    private static final Set<String> sentToFetch = new HashSet<>();
    private static final Map<String, Integer> assetSizes = new HashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    JedisPool jedisPool;

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
    // feeds can be parsed in a different way (i.e. RSS XML) to pull out links
    // application/atom+xml
    //
    // the current implementation will be a default

    @Override
    public boolean doWork() {

        try {
            // get page content from S3
            String bucketName = config.getString(BUCKET_NAME_CONFIG_KEY);
            String pageContentRef = parseInput.getAttribute(PageAttribute.PAGE_CONTENT_REF).toString();
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, pageContentRef);

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

            // get links without in-page anchor links
            // Note: this breaks for angular apps but whatever
            Set<String> outLinks = document.getElementsByTag("a")
                    .stream()
                    // Ignore rel="nofollow" links
                    .filter(elem -> !StringUtils.equalsIgnoreCase("nofollow", elem.attr("rel")))
                    .map(elem -> elem.attr("href"))
                    .filter(StringUtils::isNotBlank)
                    // Remove javascript links or in-page anchors
                    .filter(link -> !StringUtils.startsWith(link, "#"))
                    .filter(link -> !StringUtils.startsWith(link, "javascript:"))
                    .collect(Collectors.toSet());
            builder.withAttribute(PageAttribute.OUT_LINKS_RAW, outLinks);

            outLinks = normalizeLinks(outLinks);
            builder.withAttribute(PageAttribute.OUT_LINKS, outLinks);

            // get asset links
            // link tags (href)
            Set<String> cssLinks = collectLinks(document, "link", "href");
            builder.withAttribute(PageAttribute.CSS_LINKS_RAW, cssLinks);

            cssLinks = normalizeLinks(cssLinks);
            builder.withAttribute(PageAttribute.CSS_LINKS, cssLinks);

            // script tags (src)
            Set<String> jsLinks = collectLinks(document, "script", "src");
            builder.withAttribute(PageAttribute.JS_LINKS_RAW, jsLinks);

            jsLinks = normalizeLinks(jsLinks);
            builder.withAttribute(PageAttribute.JS_LINKS, jsLinks);

            // img tags (src)
            Set<String> imgLinks = collectLinks(document, "img", "src");
            builder.withAttribute(PageAttribute.IMG_LINKS_RAW, imgLinks);

            imgLinks = normalizeLinks(imgLinks);
            builder.withAttribute(PageAttribute.IMG_LINKS, imgLinks);

            // retrieve all assets and calculate total page size
            int totalPageSize = pageContentSize;
            List<String> assetLinks = new ArrayList<>(cssLinks.size() + jsLinks.size() + imgLinks.size());
            assetLinks.addAll(cssLinks);
            assetLinks.addAll(jsLinks);
            assetLinks.addAll(imgLinks);

            for (String link : assetLinks) {
                // TODO: Metrics on all asset load times as well
                // it's hard to tell what's necessary to render above the fold, but we can add it all together....
                Integer assetSize = getCachedAssetSize(link);

                if (assetSize == null && link != null) {
                    //logger.info("Normalized URI. Original: " + link + " | Normalized: " + norm);

                    final Response response = ClientBuilder.newClient().target(link).request().buildGet().invoke();

                    // read the response into a String to guarantee we get a length
                    // rather than rely on a server returning a Content-Length header
                    assetSize = response.readEntity(String.class).length();

                    setCachedAssetSize(link, assetSize);

                    // TODO: Parse CSS and pull in any referenced images and external css
                }

                if (assetSize != null) {
                    totalPageSize += assetSize;
                }
            }

            builder.withAttribute(PageAttribute.SIZE_WITH_ASSETS, totalPageSize);

            // Collect all the outbound links by content type to filter out any unwanted things
            // e.g. images
            final Map<String, List<String>> linksByContentType = outLinks.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.groupingBy(
                            link -> {
                                try {
                                    final Response response = ClientBuilder.newClient().target(link).request().build("HEAD").invoke();
                                    final String contentType = response.getStringHeaders().getFirst(CONTENT_TYPE_HEADER_KEY);

                                    if (StringUtils.isNotBlank(contentType)) {
                                        return contentType;
                                    } else {
                                        return NO_CONTENT_TYPE_KEY;
                                    }

                                } catch (Exception ex) {
                                    logger.error("Could not get content type for " + link, ex);
                                    //throw new RuntimeException(ex.getMessage(), ex);
                                    return EXCEPTION_KEY;
                                }
                            }));

            // TODO: insert whatever custom parsing here

            String nextQueue = config.getString(NEXT_QUEUE_CONFIG_KEY);
            queueManager.enqueue(nextQueue, objectMapper.writeValueAsString(builder.build()));

            String fetchQueue = config.getString(FETCH_QUEUE_NAME_CONFIG_KEY);

            linksByContentType.entrySet().forEach(entry -> {

                // Skip any links that just point to images or encountered an exception
                // while performing a HEAD request.
                if (StringUtils.startsWithIgnoreCase(entry.getKey(), IMAGE_CONTENT_TYPE_PREFIX) ||
                        StringUtils.equals(entry.getKey(), EXCEPTION_KEY)) {
                    return;
                }

                entry.getValue().forEach(link -> {
                    if (alreadySentToFetch(link) ||
                            (config.getBoolean(USE_BASE_DOMAIN_CONFIG_KEY) &&
                             !DomainUtils.isBaseDomain(config.getString(BASE_DOMAIN_CONFIG_KEY), link))) {
                        return;
                    }

                    try {
                        // TODO: Investigate double messages
                        //INFO [com.widowcrawler.parse.ParseWorker:171] - Enqueuing fetch message for http://www.xoxide.com/radiator.html/casefans.html
                        //INFO [com.widowcrawler.parse.ParseWorker:171] - Enqueuing fetch message for http://www.xoxide.com/radiator.html/casefans.html
                        //INFO [com.widowcrawler.core.queue.QueueManager:61] - Message enqueued successfully. Message ID: fab3ef38-ac44-41c7-b804-e5e3b6dcbf19
                        //INFO [com.widowcrawler.core.queue.QueueManager:61] - Message enqueued successfully. Message ID: 949a7420-94cb-45ab-a11b-c36b28d4adc2
                        logger.info("Enqueuing fetch message for " + link);
                        FetchInput fetchInput = new FetchInput(link, parseInput.getAttribute(PageAttribute.ORIGINAL_URL).toString());
                        queueManager.enqueue(fetchQueue, objectMapper.writeValueAsString(fetchInput));
                        sentToFetch(link);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                });
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

    private Set<String> normalizeLinks(Set<String> links) {
        return links.stream()
                .map(link -> linkNormalizer.normalize(parseInput.getAttribute(PageAttribute.ORIGINAL_URL).toString(), link))
                .collect(Collectors.toSet());
    }

    private boolean useRemoteCache() {
        return config.getBoolean(USE_REMOTE_CACHE_CONFIG_KEY, true);
    }

    private boolean alreadySentToFetch(String link) {
        if (useRemoteCache()) {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = SENT_TO_FETCH_KEY_PREFIX + link;
                return StringUtils.isNotBlank(jedis.get(key));
            } catch (JedisConnectionException ex) {
                logger.error("Couldn't write to cache", ex);
                return false;
            }
        } else {
            return sentToFetch.contains(link);
        }
    }

    private void sentToFetch(String link) {
        if (useRemoteCache()) {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = SENT_TO_FETCH_KEY_PREFIX + link;
                jedis.set(key, "true");
            } catch (JedisConnectionException ex) {
                logger.error("Couldn't read from cache", ex);
            }
        } else {
            sentToFetch.add(link);
        }
    }

    private Integer getCachedAssetSize(String link) {
        if (useRemoteCache()) {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = ASSET_SIZE_KEY_PREFIX + link;
                String cachedValue = jedis.get(key);

                if (StringUtils.isBlank(cachedValue)) {
                    return null;
                }

                return Integer.valueOf(cachedValue);

            } catch (NumberFormatException ex) {
                logger.error("Couldn't parse asset size. Asset: " + link, ex);
                return null;
            } catch (JedisConnectionException ex) {
                logger.error("Couldn't read from cache", ex);
                return null;
            }
        } else {
            return assetSizes.get(link);
        }
    }

    private void setCachedAssetSize(String link, Integer size) {
        if (useRemoteCache()) {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = ASSET_SIZE_KEY_PREFIX + link;
                String value = size.toString();
                jedis.set(key, value);
            } catch (JedisConnectionException ex) {
                logger.error("Couldn't read from cache", ex);
            }
        } else {
            assetSizes.put(link, size);
        }
    }
}
