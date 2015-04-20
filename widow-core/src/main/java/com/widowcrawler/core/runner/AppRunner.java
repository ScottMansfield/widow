package com.widowcrawler.core.runner;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.widowcrawler.core.dispatch.Dispatcher;
import com.widowcrawler.core.module.ConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Mansfield
 */
public class AppRunner {

    private static Logger logger = LoggerFactory.getLogger(AppRunner.class);

    public static void run(Class<? extends AbstractModule> moduleClass, String appName) throws Exception {
        Injector injector = LifecycleInjector.builder()
                .withModuleClass(moduleClass)
                .withBootstrapModule(new ConfigModule(appName))
                .usingBasePackages("com.widowcrawler")
                .build()
                .createInjector();

        LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
        lifecycleManager.start();

        Dispatcher dispatcher = injector.getInstance(Dispatcher.class);
        boolean cont = true;

        while (cont) {
            logger.info("Dispatching...");
            cont = dispatcher.dispatch();
        }

        lifecycleManager.close();
    }
}
