package com.widowcrawler.core.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.widowcrawler.core.retry.Retry;

import java.util.concurrent.*;

/**
 * @author Scott Mansfield
 */
public class WidowCoreModule extends AbstractModule {
    @Override
    protected void configure() {

        // TODO: Make region config
        AmazonSQSAsyncClient amazonSQSAsyncClient = new AmazonSQSAsyncClient();
        amazonSQSAsyncClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        bind(AmazonSQSAsyncClient.class).toInstance(amazonSQSAsyncClient);

        AmazonS3 amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
        bind(AmazonS3.class).toInstance(amazonS3Client);

        // TODO: Make number of threads in thread pool config
        // 10 workers plus 2 queue management threads
        // queue size the same as the worker threads
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(12, 12, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10));
        threadPoolExecutor.prestartAllCoreThreads();

        bind(ExecutorService.class).toInstance(threadPoolExecutor);
        bind(ThreadPoolExecutor.class).toInstance(threadPoolExecutor);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        bind(ObjectMapper.class).toInstance(mapper);
    }
}
