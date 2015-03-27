package com.widowcrawler.fetch;

import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.parse.model.ParseInput;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class FetchWorker implements Worker {

    @Inject
    QueueManager queueManager;

    private String target;

    public FetchWorker() { }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public void run() {
        Client client = ClientBuilder.newClient();

        Invocation invocation = client.target(this.target).request().buildGet();

        // TODO: can I get more accurate timing from the response object?
        long startTime = System.nanoTime();
        Response response = invocation.invoke();
        double requestDuration = (System.nanoTime() - startTime) / 1000D;

        // Massage the headers into a more usable form
        MultivaluedHashMap<String, String> stringHeaders = (MultivaluedHashMap<String, String>) response.getStringHeaders();
        Map<String, List<String>> headerMap = new HashMap<>(stringHeaders.keySet().size());

        stringHeaders.keySet().forEach( key -> headerMap.put(key, stringHeaders.get(key)) );

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
    }
}
