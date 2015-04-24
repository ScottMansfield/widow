package com.widowcrawler.analyze;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.widowcrawler.analyze.module.WidowAnalyzeModule;
import com.widowcrawler.analyze.resources.TestResources;
import com.widowcrawler.core.module.ConfigModule;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * @author Scott Mansfield
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final URI BASE_URI = URI.create("http://localhost:8080/");
    public static final String ROOT_PATH = "test";

    public static void main(String[] args) {
        try {

            Injector injector = LifecycleInjector.builder()
                    .withModuleClass(WidowAnalyzeModule.class)
                    .withBootstrapModule(new ConfigModule("widow-analyze"))
                    .usingBasePackages("com.widowcrawler")
                    .build()
                    .createInjector();

            LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
            lifecycleManager.start();

            TestResources testResources = injector.getInstance(TestResources.class);

            final ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.registerInstances(testResources);

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig);

            System.out.println(String.format("Application started.\nTry out %s%s\nHit enter to stop it...",
                    BASE_URI, ROOT_PATH));

            while (System.in.read() != 32);
            server.shutdownNow();
        } catch (Exception ex) {
            logger.error("Error: " + ex.getMessage(), ex);
        }
    }
}
