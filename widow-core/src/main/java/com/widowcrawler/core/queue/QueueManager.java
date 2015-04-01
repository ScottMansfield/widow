package com.widowcrawler.core.queue;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
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

    @Inject
    private AmazonSQSAsyncClient sqsClient;

    private Map<String, ConcurrentLinkedQueue<Message>> messagesMap = null;
    private Map<String, String> queueUrls = null;

    private ExecutorService executorService = null;
    private Queue<Future<SendMessageResult>> enqueueActions = null;

    private Runnable enququer = () -> {

        // TODO: Impose some sort of limit on the number of retries per message
        // dead-letter queue for sure, probably through AWS config

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

    public QueueManager() { }

    @PostConstruct
    public void postConstruct() {
        // configuration ftw
        // for each configured queue set up the data structure to manage the current message batch
        // async fetch messages
        // still need a better story around credentials for the sqs client
        // Also should probably have a custom message type so we can support local in-memory queues as well as SQS

        String queuesProperty = StringUtils.trim(System.getProperty("com.widowcrawler.queues"));
        Validate.notEmpty(queuesProperty);

        String[] queues = StringUtils.split(queuesProperty, "| ");

        // Initialize the message and queue URLs
        Map<String, ConcurrentLinkedQueue<Message>> tempMessagesMap = new HashMap<>(queues.length);
        Map<String, String> tempQueueUrls = new HashMap<>(queues.length);

        for (String queue : queues) {
            queue = StringUtils.trim(queue);
            String queueUrl;

            try {
                queueUrl = sqsClient.getQueueUrl(queue).getQueueUrl();
            } catch (QueueDoesNotExistException ex) {
                queueUrl = sqsClient.createQueue(queue).getQueueUrl();
            }

            tempMessagesMap.put(queue, new ConcurrentLinkedQueue<>());
            tempQueueUrls.put(queue, queueUrl);
        }

        messagesMap = Collections.unmodifiableMap(tempMessagesMap);
        queueUrls = Collections.unmodifiableMap(tempQueueUrls);

        // And finally initialize the enqueue actions pending
        enqueueActions = new ConcurrentLinkedQueue<>();

        // Start the async operations
        executorService = Executors.newFixedThreadPool(2);
        executorService.submit(messagePoller);
        executorService.submit(enququer);
    }

    @PreDestroy
    public void preDestroy() {
        executorService.shutdownNow();
    }

    /**
     * Gets the next message in the queue, or <i>null</i> for queues with no ready messages
     *
     * @param queueName the name of the queue
     * @return the mext message in the queue
     */
    public Message nextMessage(String queueName) {
        if (messagesMap.get(queueName) != null) {
            return messagesMap.get(queueName).poll();
        }

        return null;
    }

    public void confirmReceipt(String queueName, String receiptHandle) {
        sqsClient.deleteMessage(queueUrls.get(queueName), receiptHandle);
    }

    public void enqueue(String queueName, String messageBody) {
        // TODO: split metadata and message body if too large

        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueName, messageBody);
        Future<SendMessageResult> resultFuture = sqsClient.sendMessageAsync(sendMessageRequest);

        enqueueActions.add(resultFuture);
    }
}
