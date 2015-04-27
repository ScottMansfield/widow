package com.widowcrawler.core.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.inject.AbstractModule;
import com.netflix.archaius.Config;

import javax.inject.Inject;

/**
 * @author Scott Mansfield
 */
public class DynamoDBModule extends AbstractModule {

    private static final String REGION_CONFIG_KEY = "com.widowcrawler.aws.region";

    @Inject
    Config config;

    @Override
    protected void configure() {

        Region region = Region.getRegion(config.get(Regions.class, REGION_CONFIG_KEY));

        AmazonDynamoDB dynamoDBClient = new AmazonDynamoDBClient();
        dynamoDBClient.setRegion(region);

        bind(AmazonDynamoDB.class).toInstance(dynamoDBClient);
    }
}
