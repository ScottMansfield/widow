package com.widowcrawler.parse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.queue.Message;
import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.worker.NoOpWorkerProvider;
import com.widowcrawler.core.worker.QueueCleanupCallback;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.parse.model.ParseInput;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * @author Scott Mansfield
 */
@Singleton
public class ParseWorkerProvider extends WorkerProvider {

    // TODO: this really ought to be config
    private static final String QUEUE_NAME = "widow-parse";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    NoOpWorkerProvider noOpWorkerProvider;

    @Override
    public Worker get() {

        try {
            Message message = queueClient.nextMessage(QUEUE_NAME);

            ParseInput parseInput = objectMapper.readValue(message.getBody(), ParseInput.class);

            return new ParseWorker(parseInput, new QueueCleanupCallback(queueClient, QUEUE_NAME, message.getReceiptHandle()));
        } catch (IOException ex) {
            return noOpWorkerProvider.get();
        }
    }
}
