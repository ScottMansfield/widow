package com.widowcrawler.parse;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.worker.NoOpWorkerProvider;
import com.widowcrawler.core.queue.QueueManager;
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

    @Inject
    ObjectMapper objectMapper;

    @Inject
    QueueManager queueManager;

    @Inject
    NoOpWorkerProvider noOpWorkerProvider;

    @Override
    public Worker get() {

        try {
            Message message = queueManager.nextMessage("parse");

            ParseInput parseInput = objectMapper.readValue(message.getBody(), ParseInput.class);

            return new ParseWorker(parseInput);
        } catch (IOException ex) {
            return noOpWorkerProvider.get();
        }
    }
}
