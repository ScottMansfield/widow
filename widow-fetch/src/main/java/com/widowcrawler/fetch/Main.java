package com.widowcrawler.fetch;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.widowcrawler.core.dispatch.Dispatcher;
import com.widowcrawler.fetch.module.WidowFetchModule;

/**
 * @author Scott Mansfield
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // depending on system properties, run a different piece of code

        Injector injector = LifecycleInjector.builder()
                .withModuleClass(WidowFetchModule.class)
                .usingBasePackages("com.widowcrawler")
                .build()
                .createInjector();

        LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
        lifecycleManager.start();

        Dispatcher dispatcher = injector.getInstance(Dispatcher.class);
        boolean cont = true;

        while (cont) {
            cont = dispatcher.dispatch();
        }

        lifecycleManager.close();
    }
}
