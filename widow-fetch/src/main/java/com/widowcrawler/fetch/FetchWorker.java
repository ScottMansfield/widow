package com.widowcrawler.fetch;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.retry.Retry;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.model.ParseInput;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Scott Mansfield
 */
public class FetchWorker extends Worker {
    // TODO: make sure the If-Modified-Since and ETag work

    private static final Logger logger = LoggerFactory.getLogger(FetchWorker.class);

    // TODO: This really needs to be config
    private static final String PARSE_QUEUE = "widow-parse";

    // TODO: This really needs to be config
    private static final String BUCKET_NAME = "widow-test";

    @Inject
    QueueManager queueManager;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Retry retry;

    @Inject
    AmazonS3 amazonS3Client;

    private String target;

    public FetchWorker() { }

    public FetchWorker withTarget(String target) {
        this.target = target;
        return this;
    }

    @Override
    public boolean doWork() {
        try {
            Invocation invocation = ClientBuilder.newClient().target(this.target).request().buildGet();

            // TODO: can I get more accurate timing from the response object?
            long startTime = System.nanoTime();
            Response response = invocation.invoke();
            double requestDuration = (System.nanoTime() - startTime) / 1000D;

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

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    BUCKET_NAME,
                    pageContentRef,
                    new ByteArrayInputStream(pageBody.getBytes()),
                    objectMetadata);

            final PutObjectResult putObjectResult = retry.retry(() -> amazonS3Client.putObject(putObjectRequest));

            logger.info("S3 put success. Object ID: " + pageContentRef);

            ParseInput parseInput = new ParseInput.Builder()
                    .withAttribute(PageAttribute.ORIGINAL_URL, this.target)
                    .withAttribute(PageAttribute.PAGE_CONTENT_REF, pageContentRef)
                    .withAttribute(PageAttribute.HEADERS, headerMap)
                    .withAttribute(PageAttribute.STATUS_CODE, response.getStatus())
                    .withAttribute(PageAttribute.LOCALE, response.getLanguage())
                    .withAttribute(PageAttribute.TIME_ACCESSED, new DateTime(response.getDate()))
                    .withAttribute(PageAttribute.LOAD_TIME_MILLIS, requestDuration)
                    .withAttribute(PageAttribute.RESPONSE_SIZE, responseLength)
                    .build();

            this.queueManager.enqueue(PARSE_QUEUE, objectMapper.writeValueAsString(parseInput));

            return true;

        } catch (Exception ex) {
            logger.error("Exception while fetching", ex);
            return false;
        }
    }
}
