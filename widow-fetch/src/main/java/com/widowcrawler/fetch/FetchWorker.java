package com.widowcrawler.fetch;

import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.parse.model.ParseInput;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * @author Scott Mansfield
 */
public class FetchWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(FetchWorker.class);

    @Inject
    QueueManager queueManager;

    private String target;

    public FetchWorker(String target, BooleanSupplier callback) {
        super(callback);
        this.target = target;
    }

    @Override
    public void doWork() {
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

            ParseInput parseInput = new ParseInput.Builder()
                    .withPageContent(response.getEntity().toString())
                    .withHeaders(headerMap)
                    .withStatusCode(response.getStatus())
                    .withLocale(response.getLanguage())
                    .withTimeAccessed(new DateTime(response.getDate()))
                    .withLoadTimeMillis(requestDuration)
                    .withResponseSizeBytes(response.getLength())
                    .build();

            // TODO: enqueue, with option to break apart page body from metadata
            // background: sqs data size limits will eff up your day if your page is > ~255kb
            // then... cry
        } catch (Exception ex) {
            logger.error("Exception while fetching", ex);
        }
    }
}
