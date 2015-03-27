package com.widowcrawler.fetch.module;

import com.google.inject.AbstractModule;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.fetch.FetchWorkerProvider;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class})
public class WidowFetchModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkerProvider.class).to(FetchWorkerProvider.class).asEagerSingleton();
    }
}
