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

        // TODO: Make region config
        AmazonSQSAsyncClient amazonSQSAsyncClient = new AmazonSQSAsyncClient();
        amazonSQSAsyncClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        bind(AmazonSQSAsyncClient.class).toInstance(amazonSQSAsyncClient);

        AmazonS3 amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
        bind(AmazonS3.class).toInstance(amazonS3Client);

        bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        bind(ObjectMapper.class).toInstance(mapper);

        bind(Retry.class).asEagerSingleton();
    }
}
