package com.widowcrawler.index.module;

import com.google.inject.AbstractModule;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.DynamoDBModule;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.index.IndexWorkerProvider;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class, DynamoDBModule.class})
public class WidowIndexModule extends AbstractModule {
    @Override
    public void configure() {
        bind(WorkerProvider.class).to(IndexWorkerProvider.class).asEagerSingleton();
    }
}
