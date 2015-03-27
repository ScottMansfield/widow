package com.widowcrawler.core.queue;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.Message;
import com.netflix.governator.annotations.AutoBindSingleton;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Scott Mansfield
 */
@AutoBindSingleton
public class QueueManager {

    @Inject
    private AmazonSQSAsyncClient sqsClient;

    private final Map<String, ConcurrentLinkedQueue<Message>> messagesMap;

    public QueueManager() {
        // configuration ftw
        // for each configured queue set up the data structure to manage the current message batch
        // async fetch messages
        // still need a better story around credentials for the sqs client
        // Also should probably have a custom message type so we can support local in-memory queues as well as SQS

        String[] queues = StringUtils.split(System.getProperty("com.widowcrawler.queues"), "| ");

        ConcurrentMap<String, ConcurrentLinkedQueue<Message>> tempMap = new ConcurrentHashMap<>();

        if (queues != null) {
            for (String queue : queues) {
                tempMap.put(queue, new ConcurrentLinkedQueue<Message>());
            }
        }

        messagesMap = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Gets the next message in the queue, or <i>null</i> for the
     *
     * @param queueName
     * @return
     */
    public Message nextMessage(String queueName) {
        return messagesMap.get(queueName).poll();
    }

    // TODO: enqueue function with generic ability to split metadata and message body if too large
    // too large being configurable and the splitting being optional(?)
}
