package com.widowcrawler.core.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;
import com.netflix.archaius.Config;

import javax.inject.Inject;

/**
 * @author smansfield
 */
public class S3Module extends AbstractModule {

    private static final String REGION_CONFIG_KEY = "com.widowcrawler.aws.region";

    @Inject
    private Config config;

    @Override
    protected void configure() {

        Region region = Region.getRegion(config.get(Regions.class, REGION_CONFIG_KEY));

        AmazonS3 amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(region);
        bind(AmazonS3.class).toInstance(amazonS3Client);
    }
}
