package com.widowcrawler.analyze;

import com.google.inject.servlet.GuiceFilter;
import com.widowcrawler.analyze.startup.WidowAnalyzeServletContextListener;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.net.URI;
import java.util.EnumSet;

/**
 * @author Scott Mansfield
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final URI BASE_URI = URI.create("http://localhost:8080/REST/");
    public static final String ROOT_PATH = "test/ping";

    public static void main(String[] args) {
        try {

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, false);

            // Creating a webapp context for the resources / DI container
            final WebappContext webappContext = new WebappContext("Widow analyze");

            webappContext.addListener(new WidowAnalyzeServletContextListener());

            ServletRegistration servletRegistration = webappContext.addServlet("ServletContainer", ServletContainer.class);
            servletRegistration.addMapping("/REST/*");
            servletRegistration.setInitParameter("javax.ws.rs.Application",
                    "com.widowcrawler.analyze.startup.WidowAnalyzeResourceConfig");

            final FilterRegistration registration = webappContext.addFilter("GuiceFilter", GuiceFilter.class);
            registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/REST/*");

            webappContext.deploy(server);

            // static assets
            CLStaticHttpHandler clStaticHttpHandler = new CLStaticHttpHandler(
                    Main.class.getClassLoader(),
                    "/", "/lib/", "/js/", "/css/", "/templates/");
            server.getServerConfiguration().addHttpHandler(clStaticHttpHandler);

            server.start();

            System.out.println(String.format("Application started.\nTry out %s%s\nHit enter to stop it...",
                    BASE_URI, ROOT_PATH));

            while(System.in.read() != 32);

            server.shutdownNow();
        } catch (Exception ex) {
            logger.error("Error: " + ex.getMessage(), ex);
        }
    }
}
