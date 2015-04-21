package com.widowcrawler.core.queue;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.netflix.archaius.Config;
import com.netflix.governator.annotations.AutoBindSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;

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

    private Map<String, LinkedBlockingQueue<Message>> messagesMap = null;
    private Map<String, String> queueUrls = null;

    private Queue<Future<SendMessageResult>> enqueueActions = null;

    private Runnable enququer = () -> {

        //noinspection InfiniteLoopStatement
        while (true) {
            Future<SendMessageResult> future = null;

            while (future == null) {
                future = enqueueActions.poll();

                if (future == null) {
                    Thread.yield();
                }
            }

            try {
                SendMessageResult result = future.get();
                logger.info("Message enqueued successfully. Message ID: " + result.getMessageId());

            } catch (InterruptedException e) {
                logger.error("Enqueuer interrupted", e);
                Thread.currentThread().interrupt();

            } catch (ExecutionException e) {
                // eat the error, because wtf else
                logger.error("Enqueueing failed", e.getCause());
            }
        }
    };

    private Runnable messagePoller = () -> {
        // if < 10 messages already in queue && no in-progress fetch, fetch
        // else no

        //noinspection InfiniteLoopStatement
        while (true) {
            for (Map.Entry<String, String> entry : queueUrls.entrySet()) {
                String queueName = entry.getKey();
                String queueUrl = entry.getValue();

                if (messagesMap.get(queueName).size() < 1) {
                    // pull new messages in batches with long-polling enabled
                    ReceiveMessageRequest rmr = new ReceiveMessageRequest(queueUrl)
                            .withMaxNumberOfMessages(10)
                            .withWaitTimeSeconds(10);
                    List<com.amazonaws.services.sqs.model.Message> messages = sqsClient.receiveMessage(rmr).getMessages();

                    // Convert Amazon SQS message to our general message type
                    for (com.amazonaws.services.sqs.model.Message message : messages) {
                        Message genericMessage = new Message(message.getBody(), message.getMessageId(), message.getReceiptHandle());
                        messagesMap.get(queueName).offer(genericMessage);
                    }
                }
            }

            Thread.yield();
        }
    };

    @PostConstruct
    public void postConstruct() {
        // configuration ftw
        // for each configured queue set up the data structure to manage the current message batch
        // async fetch messages
        // still need a better story around credentials for the sqs client
        // Also should probably have a custom message type so we can support local in-memory queues as well as SQS

        //String queuesProperty = StringUtils.trim(System.getProperty("com.widowcrawler.queues"));
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

        // And finally initialize the enqueue actions pending
        enqueueActions = new ConcurrentLinkedQueue<>();

        // Start the async operations
        executorService.submit(messagePoller);
        executorService.submit(enququer);
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
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueName, messageBody);
        Future<SendMessageResult> resultFuture = sqsClient.sendMessageAsync(sendMessageRequest);

        enqueueActions.add(resultFuture);
    }
}
