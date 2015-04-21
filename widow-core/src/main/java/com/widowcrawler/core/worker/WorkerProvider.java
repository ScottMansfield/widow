package com.widowcrawler.core.worker;

import com.netflix.archaius.Config;
import com.widowcrawler.core.queue.QueueManager;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Scott Mansfield
 */
public abstract class WorkerProvider implements Provider<Worker> {

    protected static final String QUEUE_NAME_CONFIG_KEY = "com.widowcrawler.queue.read";

    @Inject
    protected Config config;

    @Inject
    protected QueueManager queueClient;

}
