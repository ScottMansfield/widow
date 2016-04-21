package com.widowcrawler.fetch;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.archaius.Config;
import com.widowcrawler.core.model.FetchInput;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.model.ParseInput;
import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.siteattr.RobotsTxtManager;
import com.widowcrawler.core.util.DomainUtils;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.terminator.model.RobotsTxt;
import com.widowcrawler.terminator.model.RuleType;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.widowcrawler.core.retry.Retry.retry;

/**
 * @author Scott Mansfield
 */
public class FetchWorker extends Worker {
    // TODO: make sure the If-Modified-Since and ETag work

    private static final Logger logger = LoggerFactory.getLogger(FetchWorker.class);

    private static final String NEXT_QUEUE_CONFIG_KEY = "com.widowcrawler.queue.next";
    private static final String BUCKET_NAME_CONFIG_KEY = "com.widowcrawler.bucket.name";
    private static final String USE_BASE_DOMAIN_CONFIG_KEY = "com.widowcrawler.use.base.domain";
    private static final String BASE_DOMAIN_CONFIG_KEY = "com.widowcrawler.base.domain";

    @Inject
    QueueManager queueManager;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AmazonS3 amazonS3Client;

    private FetchInput input;

    public FetchWorker() { }

    public FetchWorker withInput(FetchInput input) {
        this.input = input;
        return this;
    }

    @Override
    public boolean doWork() {
        try {

            final RobotsTxt robotsTxt = RobotsTxtManager.getByDomain(new URL(input.getUrl()).getHost());

            // FIXME: HORRIBLE, HACKY, NO-GOOD BADNESS
            boolean allowed = robotsTxt.getRuleSets().get("*").stream().anyMatch(rule -> {
                try {
                    //logger.info("Rule} " + rule.getRuleType() + ": " + rule.getPathMatch());
                    return rule.getRuleType() == RuleType.DISALLOW &&
                            StringUtils.equalsIgnoreCase(rule.getPathMatch(), new URL(input.getUrl()).getPath());

                } catch (Exception ex) {
                    logger.error("Exception while evaluating robots.txt allowed-ness");
                    return false;
                }
            });

            if (!allowed) {
                logger.info("URL " + input.getUrl() + " is not allowed by the robots.txt");
                return true;
            }
            // END HORRIBLE, HACKY, NO-GOOD BADNESS

            if (config.getBoolean(USE_BASE_DOMAIN_CONFIG_KEY) &&
                !DomainUtils.isBaseDomain(config.getString(BASE_DOMAIN_CONFIG_KEY), this.input.getUrl())) {
                logger.warn("Rejecting message because it goes not have the right base domain:\n" +
                                "\tUrl: " + input.getUrl() + "\n" +
                                "\tReferrer: " + input.getReferrer());
                return true;
            }

            Invocation invocation = ClientBuilder.newClient()
                    .target(this.input.getUrl())
                    .request()
                    .header("User-Agent", "Widow Crawler (http://widowcrawler.com)")
                    .buildGet();

            // TODO: can I get more accurate timing from the response object?
            long startTime = System.nanoTime();
            Response response = invocation.invoke();
            double requestDuration = (System.nanoTime() - startTime) / 1_000_000D;

            // Massage the headers into a more usable form
            MultivaluedMap<String, String> stringHeaders = response.getStringHeaders();
            Map<String, List<String>> headerMap = new HashMap<>(stringHeaders.keySet().size());

            stringHeaders.keySet().forEach(key -> headerMap.put(key, stringHeaders.get(key)));

            String pageBody = response.readEntity(String.class);

            // Use the Content-Length header, with a backup of reading the entity and getting
            // the length of that
            int responseLength = response.getLength();
            if (responseLength == -1) {
                responseLength = pageBody.length();
            }

            String pageContentRef = UUID.randomUUID().toString();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(pageBody.getBytes().length);

            String bucketName = config.getString(BUCKET_NAME_CONFIG_KEY);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    pageContentRef,
                    new ByteArrayInputStream(pageBody.getBytes()),
                    objectMetadata);

            final PutObjectResult putObjectResult = retry(() -> amazonS3Client.putObject(putObjectRequest));

            logger.info("S3 put success. Object ID: " + pageContentRef + " | Content MD5: " + putObjectResult.getContentMd5());

            ParseInput parseInput = new ParseInput.Builder()
                    .withAttribute(PageAttribute.ORIGINAL_URL, this.input.getUrl())
                    .withAttribute(PageAttribute.REFERRER, this.input.getReferrer())
                    .withAttribute(PageAttribute.PAGE_CONTENT_REF, pageContentRef)
                    .withAttribute(PageAttribute.HEADERS, headerMap)
                    .withAttribute(PageAttribute.STATUS_CODE, response.getStatus())
                    .withAttribute(PageAttribute.LOCALE, response.getLanguage())
                    .withAttribute(PageAttribute.TIME_ACCESSED, new DateTime(response.getDate()))
                    .withAttribute(PageAttribute.LOAD_TIME_MILLIS, requestDuration)
                    .withAttribute(PageAttribute.RESPONSE_SIZE, responseLength)
                    .build();

            String nextQueue = config.getString(NEXT_QUEUE_CONFIG_KEY);
            this.queueManager.enqueue(nextQueue, objectMapper.writeValueAsString(parseInput));

            return true;

        } catch (Exception ex) {
            logger.error("Exception while fetching", ex);
            return false;
        }
    }
}
