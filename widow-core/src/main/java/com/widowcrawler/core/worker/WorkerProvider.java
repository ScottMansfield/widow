package com.widowcrawler.core.worker;

import com.widowcrawler.core.queue.QueueManager;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Scott Mansfield
 */
public abstract class WorkerProvider implements Provider<Worker> {

    @Inject
    protected QueueManager queueClient;

}
