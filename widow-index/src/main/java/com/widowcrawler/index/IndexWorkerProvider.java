package com.widowcrawler.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.IndexInput;
import com.widowcrawler.core.queue.Message;
import com.widowcrawler.core.worker.NoOpWorkerProvider;
import com.widowcrawler.core.worker.QueueCleanupCallback;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * @author Scott Mansfield
 */
public class IndexWorkerProvider extends WorkerProvider {

    private static Logger logger = LoggerFactory.getLogger(IndexWorkerProvider.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    NoOpWorkerProvider noOpWorkerProvider;

    @Inject
    Provider<IndexWorker> seed;

    @Override
    public Worker get() {
        try {
            String pullQueue = config.getString(QUEUE_NAME_CONFIG_KEY);

            Message message = queueClient.nextMessage(pullQueue);

            IndexInput indexInput = objectMapper.readValue(message.getBody(), IndexInput.class);

            return seed.get().withInput(indexInput)
                    .withCallback(new QueueCleanupCallback(queueClient, pullQueue, message.getReceiptHandle()));

        } catch(InterruptedException ex) {
            logger.info("Thread interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            logger.error("Error parsing JSON", ex);
        }

        return noOpWorkerProvider.get();
    }
}
