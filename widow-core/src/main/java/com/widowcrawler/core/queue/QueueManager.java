package com.widowcrawler.core.queue;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.netflix.archaius.Config;
import com.netflix.governator.annotations.AutoBindSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Scott Mansfield
 */
@AutoBindSingleton
public class QueueManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String QUEUE_NAMES_PROPERTY = "com.widowcrawler.QueueManager.queueNames";

    @Inject
    private Config config;

    @Inject
    private AmazonSQSAsyncClient sqsClient;

    @Inject
    private ExecutorService executorService;

    @Inject
    private Enqueuer enqueuer;

    private Map<String, LinkedBlockingQueue<Message>> messagesMap = null;
    private Map<String, String> queueUrls = null;

    private LinkedBlockingQueue<Future<SendMessageResult>> enqueueActions = null;

    private Runnable messagePoller = () -> {
        // if < 10 messages already in queue && no in-progress fetch, fetch
        // else no

        //noinspection InfiniteLoopStatement
        while (true) {
            //queueUrls.entrySet().forEach(entry -> logger.info("Key: " + entry.getKey() + " | Value: " + entry.getValue()));
            //System.out.println("\"Jia you\" - darongyi");

            queueUrls.keySet().forEach(queueName -> {
                String queueUrl = queueUrls.get(queueName);

                if (messagesMap.get(queueName).size() < 1) {
                    // pull new messages in batches with long-polling enabled
                    ReceiveMessageRequest rmr = new ReceiveMessageRequest(queueUrl)
                            .withMaxNumberOfMessages(10)
                            .withWaitTimeSeconds(10);

                    sqsClient.receiveMessage(rmr).getMessages().forEach(msg -> {
                        Message genericMessage = new Message(msg.getBody(), msg.getMessageId(), msg.getReceiptHandle());
                        messagesMap.get(queueName).offer(genericMessage);
                    });
                }
            });

            Thread.yield();
        }
    };

    @PostConstruct
    public void postConstruct() {
        // for each configured queue set up the data structure to manage the current message batch

        String queuesProperty = StringUtils.trim(config.getString(QUEUE_NAMES_PROPERTY));
        Validate.notEmpty(queuesProperty);

        String[] queues = StringUtils.split(queuesProperty, "| ");

        // Initialize the message and queue URLs
        Map<String, LinkedBlockingQueue<Message>> tempMessagesMap = new HashMap<>(queues.length);
        Map<String, String> tempQueueUrls = new HashMap<>(queues.length);

        for (String queue : queues) {
            queue = StringUtils.trim(queue);
            String queueUrl;

            logger.info("Initializing queue " + queue);

            try {
                queueUrl = sqsClient.getQueueUrl(queue).getQueueUrl();
            } catch (QueueDoesNotExistException ex) {
                queueUrl = sqsClient.createQueue(queue).getQueueUrl();
            }

            tempMessagesMap.put(queue, new LinkedBlockingQueue<>());
            tempQueueUrls.put(queue, queueUrl);
        }

        messagesMap = Collections.unmodifiableMap(tempMessagesMap);
        queueUrls = Collections.unmodifiableMap(tempQueueUrls);

        // Start the async operation
        executorService.submit(messagePoller);
    }

    /**
     * Gets the next message in the queue, or blocks until one becomes available
     *
     * @param queueName the name of the queue
     * @return the mext message in the queue
     * @throws java.lang.IllegalArgumentException when the queue is not set up
     */
    public Message nextMessage(String queueName) throws InterruptedException {
        if (messagesMap.get(queueName) != null) {
            return messagesMap.get(queueName).take();
        }

        throw new IllegalArgumentException("Queue " + queueName + " is not set up in the QueueManager");
    }

    /**
     * Confirms the receipt of a message. Can be used before or after processing, but care must be
     * taken to avoid losing messages.
     *
     * @param queueName the name of the queue
     * @param receiptHandle the receipt handle on the original message
     * @throws java.lang.IllegalArgumentException when th queue is not set up
     */
    public void confirmReceipt(String queueName, String receiptHandle) {
        if (queueUrls.get(queueName) == null) {
            throw new IllegalArgumentException("Queue " + queueName + " is not set up in the QueueManager");
        }

        sqsClient.deleteMessage(queueUrls.get(queueName), receiptHandle);
    }

    public void enqueue(String queueName, String messageBody) {
        enqueuer.enqueue(queueName, messageBody);
    }
}
