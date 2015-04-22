package com.widowcrawler.parse.module;

import com.google.inject.AbstractModule;
import com.netflix.archaius.Config;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.parse.ParseWorkerProvider;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class})
public class WidowParseModule extends AbstractModule {

    private static final String CACHE_ENDPOINT_CONFIG_KEY = "com.widowcrawler.cache.endpoint";
    private static final String CACHE_PORT_CONFIG_KEY = "com.widowcrawler.cache.port";

    @Inject
    Config config;

    @Override
    protected void configure() {

        String cacheHostName = config.getString(CACHE_ENDPOINT_CONFIG_KEY);
        Integer port = config.getInteger(CACHE_PORT_CONFIG_KEY, 6379);
        JedisPool pool = new JedisPool(new JedisPoolConfig(), cacheHostName);
        bind(JedisPool.class).toInstance(pool);

        bind(WorkerProvider.class).to(ParseWorkerProvider.class).asEagerSingleton();
    }
}
