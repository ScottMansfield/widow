package com.widowcrawler.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.FetchInput;
import com.widowcrawler.core.queue.Message;
import com.widowcrawler.core.worker.NoOpWorkerProvider;
import com.widowcrawler.core.worker.QueueCleanupCallback;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * @author Scott Mansfield
 */
@Singleton
public class FetchWorkerProvider extends WorkerProvider {

    private static final String QUEUE_NAME = "widow-fetch";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    ObjectMapper objectMapper;

    @Inject
    NoOpWorkerProvider noOpWorkerProvider;

    @Inject
    Provider<FetchWorker> seed;

    /**
     * Gets the next worker to fetch page content
     *
     * @return the next {@link com.widowcrawler.fetch.FetchWorker} that will fetch content
     */
    @Override
    public Worker get() {
        try {
            Message message = queueClient.nextMessage(QUEUE_NAME);

            logger.info("Received message: " + message.getBody());

            FetchInput target = objectMapper.readValue(message.getBody(), FetchInput.class);

            return seed.get().withTarget(target.getUrl())
                    .withCallback(new QueueCleanupCallback(queueClient, QUEUE_NAME, message.getReceiptHandle()));

        } catch(InterruptedException ex) {
            logger.info("Thread interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            // TODO: dead-letter queue?
            logger.error("Error parsing JSON", ex);
        }

        return noOpWorkerProvider.get();
    }
}
