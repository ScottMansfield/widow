package com.widowcrawler.core.worker;

import com.widowcrawler.core.queue.QueueManager;

import java.util.function.BooleanSupplier;

/**
 * @author Scott Mansfield
 */
public class QueueCleanupCallback implements BooleanSupplier {

    private QueueManager queueManager;
    private String queueName;
    private String receiptHandle;

    public QueueCleanupCallback(QueueManager queueManager, String queueName, String receiptHandle) {
        this.queueManager = queueManager;
        this.queueName = queueName;
        this.receiptHandle = receiptHandle;
    }

    @Override
    public boolean getAsBoolean() {
        queueManager.confirmReceipt(queueName, receiptHandle);
        return true;
    }
}
