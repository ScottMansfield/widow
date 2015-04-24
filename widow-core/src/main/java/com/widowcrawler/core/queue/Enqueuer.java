package com.widowcrawler.core.queue;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Scott Mansfield
 */
public class Enqueuer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Enqueuer.class);

    @Inject
    AmazonSQSAsyncClient sqsClient;

    @Inject
    ExecutorService executorService;

    private static class SendMessageRequestHolder {
        private SendMessageRequest sendMessageRequest;
        private Future<SendMessageResult> sendMessageResultFuture;
        private int tries;

        public SendMessageRequestHolder(
                SendMessageRequest sendMessageRequest,
                Future<SendMessageResult> sendMessageResultFuture,
                int tries) {
            this.sendMessageRequest = sendMessageRequest;
            this.sendMessageResultFuture = sendMessageResultFuture;
            this.tries = tries;
        }

        public SendMessageRequest getSendMessageRequest() {
            return sendMessageRequest;
        }

        public Future<SendMessageResult> getSendMessageResultFuture() {
            return sendMessageResultFuture;
        }

        public int getTries() {
            return tries;
        }
    }

    private final LinkedBlockingQueue<SendMessageRequestHolder> enqueueActions = new LinkedBlockingQueue<>();

    @PostConstruct
    public void postConstruct() {
        executorService.submit(this);
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            SendMessageRequestHolder holder = null;

            try {
                holder = enqueueActions.take();
                SendMessageResult result = holder.getSendMessageResultFuture().get();
                logger.info("Message enqueued successfully. Message ID: " + result.getMessageId());

            } catch (InterruptedException e) {
                logger.error("Enqueuer interrupted", e);
                Thread.currentThread().interrupt();

            } catch (ExecutionException e) {
                logger.error("Enqueueing failed", e.getCause());

                SendMessageRequest sendMessageRequest = holder.getSendMessageRequest();

                if (holder.getTries() >= 5) {
                    String message = String.format("Giving up on message \"%s\" sent to queue %s...",
                            sendMessageRequest.getQueueUrl(),
                            sendMessageRequest.getMessageBody().substring(0, 50));
                    logger.error(message);
                } else {
                    enqueue(sendMessageRequest.getQueueUrl(),
                            sendMessageRequest.getMessageBody(),
                            holder.getTries() + 1);
                }
            }
        }
    }

    public void enqueue(String queueName, String messageBody) {
        enqueue(queueName, messageBody, 0);
    }

    private void enqueue(String queueName, String messageBody, int tries) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueName, messageBody);
        Future<SendMessageResult> resultFuture = sqsClient.sendMessageAsync(sendMessageRequest);

        enqueueActions.add(new SendMessageRequestHolder(sendMessageRequest, resultFuture, tries));
    }
}
