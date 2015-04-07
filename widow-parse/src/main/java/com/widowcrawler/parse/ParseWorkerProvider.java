package com.widowcrawler.parse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.ParseInput;
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
public class ParseWorkerProvider extends WorkerProvider {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // TODO: this really ought to be config
    private static final String PARSE_QUEUE = "widow-parse";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    NoOpWorkerProvider noOpWorkerProvider;

    @Inject
    Provider<ParseWorker> seed;

    @Override
    public Worker get() {
        try {
            Message message = queueClient.nextMessage(PARSE_QUEUE);

            ParseInput parseInput = objectMapper.readValue(message.getBody(), ParseInput.class);

            return seed.get().withInput(parseInput)
                .withCallback(new QueueCleanupCallback(queueClient, PARSE_QUEUE, message.getReceiptHandle()));

        } catch(InterruptedException ex) {
            logger.info("Thread interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            logger.error("Error parsing JSON", ex);
        }

        return noOpWorkerProvider.get();
    }
}
