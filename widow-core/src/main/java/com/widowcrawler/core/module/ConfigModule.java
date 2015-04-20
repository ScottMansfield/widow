package com.widowcrawler.core.module;

import com.netflix.archaius.AppConfig;
import com.netflix.archaius.Config;
import com.netflix.archaius.DefaultAppConfig;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;

/**
 * @author Scott Mansfield
 */
public class ConfigModule implements BootstrapModule {

    private String appName;

    public ConfigModule(String appName) {
        this.appName = appName;
    }

    @Override
    public void configure(BootstrapBinder binder) {
        final DefaultAppConfig config = DefaultAppConfig.builder()
                .withApplicationConfigName(appName)
                .build();
        binder.bind(Config.class).toInstance(config);
        binder.bind(AppConfig.class).toInstance(config);
    }
}
