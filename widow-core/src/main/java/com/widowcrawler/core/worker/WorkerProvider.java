package com.widowcrawler.core.worker;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.widowcrawler.core.queue.QueueManager;
import com.widowcrawler.core.worker.Worker;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Scott Mansfield
 */
public abstract class WorkerProvider implements Provider<Worker> {

    @Inject
    protected QueueManager queueClient;

}
