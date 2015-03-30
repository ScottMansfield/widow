package com.widowcrawler.fetch;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.queue.Message;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * @author Scott Mansfield
 */
@Singleton
public class FetchWorkerProvider extends WorkerProvider {

    private static final String QUEUE_NAME = "widow_fetch_test";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    ObjectMapper objectMapper;

    /**
     * Gets the next worker to fetch page content
     *
     * @return the next {@link com.widowcrawler.fetch.FetchWorker} that will fetch content
     */
    @Override
    public Worker get() {
        Worker worker = null;
        Message message = queueClient.nextMessage(QUEUE_NAME);

        while (message == null) {
            logger.info("Getting message...");
            message = queueClient.nextMessage(QUEUE_NAME);
        }

        logger.info("Received message?!");

        while (worker == null ) {
            try {
                String target = objectMapper.readValue(message.getBody(), String.class);
                worker = new FetchWorker(target);
            } catch (JsonParseException | JsonMappingException ex) {
                logger.error("[DROPPING MESSAGE] Could not parse message. Moving to next message in queue. Content: " + message.getBody(), ex);
                message = queueClient.nextMessage(QUEUE_NAME);
            } catch (IOException ex) {
                logger.error("IOException while parsing content. Retrying indefinitely... Content: " + message.getBody(), ex);
            }
        }

        return worker;
    }
}
