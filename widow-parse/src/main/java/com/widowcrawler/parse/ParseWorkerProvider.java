package com.widowcrawler.parse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.queue.Message;
import com.widowcrawler.core.worker.NoOpWorkerProvider;
import com.widowcrawler.core.worker.QueueCleanupCallback;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.core.model.ParseInput;
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

            // spin on the queue end
            // TODO: This can be made more efficient
            while (message == null) {
                message = queueClient.nextMessage(PARSE_QUEUE);
            }

            // TODO: more error handling
            ParseInput parseInput = objectMapper.readValue(message.getBody(), ParseInput.class);

            Worker worker = seed.get().withInput(parseInput)
                .withCallback(new QueueCleanupCallback(queueClient, PARSE_QUEUE, message.getReceiptHandle()));

            return worker;

        } catch (IOException ex) {
            logger.error("Error getting parse worker", ex);
            return noOpWorkerProvider.get();
        }
    }
}
