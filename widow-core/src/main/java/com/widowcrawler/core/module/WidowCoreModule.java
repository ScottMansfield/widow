package com.widowcrawler.core.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.netflix.archaius.Config;
import com.widowcrawler.core.dispatch.Dispatcher;
import com.widowcrawler.core.queue.Enqueuer;
import com.widowcrawler.core.queue.QueueManager;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Scott Mansfield
 */
public class WidowCoreModule extends AbstractModule {

    private static final String REGION_CONFIG_KEY = "com.widowcrawler.aws.region";

    private static final String NUM_THREADS_CONFIG_KEY = "com.widowcrawler.workers.threads";

    private static final Integer QUEUE_MANAGER_HEADROOM = 2;

    @Inject
    private Config config;

    @Override
    protected void configure() {

        Region region = Region.getRegion(config.get(Regions.class, REGION_CONFIG_KEY));

        AmazonSQSAsyncClient amazonSQSAsyncClient = new AmazonSQSAsyncClient();
        amazonSQSAsyncClient.setRegion(region);
        bind(AmazonSQSAsyncClient.class).toInstance(amazonSQSAsyncClient);

        AmazonS3 amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(region);
        bind(AmazonS3.class).toInstance(amazonS3Client);

        // Thread pool setup. Include a queue the size of the pool.
        // Always add headroom for admin in QueueManager
        int numWorkers = config.get(Integer.class, NUM_THREADS_CONFIG_KEY);
        int poolThreads = numWorkers + QUEUE_MANAGER_HEADROOM;

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolThreads, poolThreads, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(numWorkers));
        threadPoolExecutor.prestartAllCoreThreads();

        bind(ExecutorService.class).toInstance(threadPoolExecutor);
        bind(ThreadPoolExecutor.class).toInstance(threadPoolExecutor);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        bind(ObjectMapper.class).toInstance(mapper);

        bind(Dispatcher.class).asEagerSingleton();
        bind(Enqueuer.class).asEagerSingleton();
        bind(QueueManager.class).asEagerSingleton();
    }
}
