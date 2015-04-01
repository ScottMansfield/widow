package com.widowcrawler.parse.module;

import com.google.inject.AbstractModule;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.parse.ParseWorkerProvider;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class})
public class WidowParseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkerProvider.class).to(ParseWorkerProvider.class).asEagerSingleton();
    }
}
