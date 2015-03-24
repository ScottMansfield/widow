package com.widowcrawler.fetch;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.widowcrawler.core.worker.Worker;
import com.widowcrawler.core.worker.WorkerProvider;

/**
 * @author Scott Mansfield
 */
public class FetchWorkerProvider extends WorkerProvider {
    @Override
    public Worker get() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
        receiveMessageRequest.setQueueUrl(""); //no queue URL builder yet

        ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest);

        for (Message message : receiveMessageResult.getMessages()) {

        }

        return null;
    }
}
