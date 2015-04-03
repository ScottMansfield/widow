package com.widowcrawler.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.queue.QueueManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class FetchWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(FetchWorker.class);

    // TODO: This really needs to be config
    private static final String PARSE_QUEUE = "widow-parse";

    @Inject
    QueueManager queueManager;

    @Inject
    ObjectMapper objectMapper;

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

            ParseInput parseInput = new ParseInput.Builder()
                    .withOriginalURL(this.target)
                    .withPageContent(pageBody)
                    .withHeaders(headerMap)
                    .withStatusCode(response.getStatus())
                    .withAttribute(PageAttribute.LOCALE, response.getLanguage())
                    .withAttribute(PageAttribute.TIME_ACCESSED, new DateTime(response.getDate()))
                    .withAttribute(PageAttribute.LOAD_TIME_MILLIS, requestDuration)
                    .withAttribute(PageAttribute.RESPONSE_SIZE, response.getLength())
                    .build();

            this.queueManager.enqueue(PARSE_QUEUE, objectMapper.writeValueAsString(parseInput));

            return true;

        } catch (Exception ex) {
            logger.error("Exception while fetching", ex);
            return false;
        }
    }
}
