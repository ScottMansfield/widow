package com.widowcrawler.core.module;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.google.inject.AbstractModule;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Scott Mansfield
 */
public class WidowCoreModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AmazonSQSAsyncClient.class).asEagerSingleton();

        bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
    }
}
