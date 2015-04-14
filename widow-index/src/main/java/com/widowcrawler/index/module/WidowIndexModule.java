package com.widowcrawler.index.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.inject.AbstractModule;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.index.IndexWorkerProvider;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class})
public class WidowIndexModule extends AbstractModule {

    @Override
    public void configure() {
        // TODO: The region needs to be config
        AmazonDynamoDB dynamoDBClient = new AmazonDynamoDBClient();
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_WEST_2));

        bind(AmazonDynamoDB.class).toInstance(dynamoDBClient);
        bind(WorkerProvider.class).to(IndexWorkerProvider.class).asEagerSingleton();
    }
}
