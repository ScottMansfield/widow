package com.widowcrawler.analyze.startup;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.governator.guice.LifecycleInjector;
import com.widowcrawler.analyze.module.WidowAnalyzeModule;
import com.widowcrawler.core.module.ConfigModule;

/**
 * @author Scott Mansfield
 */
public class WidowAnalyzeServletContextListener extends GuiceServletContextListener {

    private static Injector injector;

    static {
        injector = LifecycleInjector.builder()
                .withModuleClass(WidowAnalyzeModule.class)
                .withBootstrapModule(new ConfigModule("widow-analyze"))
                .usingBasePackages("com.widowcrawler")
                .build()
                .createInjector();
    }

    public static Injector getCachedInjector() {
        return injector;
    }

    @Override
    protected Injector getInjector() {
        return injector;
    }
}
