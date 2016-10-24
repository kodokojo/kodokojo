package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.config.ZookeeperConfig;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.zookeeper.ZookeeperConfigurationStore;

public class ZookeeperModule extends AbstractModule {
    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    ConfigurationStore provideConfigurationStore(ZookeeperConfig zookeeperConfig) {
        return new ZookeeperConfigurationStore(zookeeperConfig);
    }
}
