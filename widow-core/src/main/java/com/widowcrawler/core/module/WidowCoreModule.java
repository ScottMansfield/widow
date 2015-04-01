package com.widowcrawler.core.module;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
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

        // Singletons
        bind(AmazonSQSAsyncClient.class).asEagerSingleton();

        // Specific instances
        bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        bind(ObjectMapper.class).toInstance(mapper);
    }
}
