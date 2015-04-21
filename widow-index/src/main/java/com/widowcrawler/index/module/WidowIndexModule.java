package com.widowcrawler.index.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.netflix.archaius.Config;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.index.IndexWorkerProvider;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class})
public class WidowIndexModule extends AbstractModule {

    private static final String REGION_CONFIG_KEY = "com.widowcrawler.aws.region";

    @Inject
    Config config;

    @Override
    public void configure() {

        Region region = Region.getRegion(config.get(Regions.class, REGION_CONFIG_KEY));

        AmazonDynamoDB dynamoDBClient = new AmazonDynamoDBClient();
        dynamoDBClient.setRegion(region);

        bind(AmazonDynamoDB.class).toInstance(dynamoDBClient);
        bind(WorkerProvider.class).to(IndexWorkerProvider.class).asEagerSingleton();
    }
}
