package com.widowcrawler.analyze.startup;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;

/**
 * @author Scott Mansfield
 */
public class WidowAnalyzeResourceConfig extends ResourceConfig {

    @Inject
    public WidowAnalyzeResourceConfig(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        packages("com.widowcrawler.analyze");

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(WidowAnalyzeServletContextListener.getCachedInjector());
    }
}
