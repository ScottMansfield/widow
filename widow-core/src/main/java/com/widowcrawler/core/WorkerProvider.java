package com.widowcrawler.core;

import com.amazonaws.services.sqs.AmazonSQSClient;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Scott Mansfield
 */
public abstract class WorkerProvider implements Provider<Worker> {

    @Inject
    protected AmazonSQSClient sqsClient;

}
