package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickConfigurerProvider;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.MarathonConfig;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.marathon.MarathonBrickManager;
import io.kodokojo.service.marathon.MarathonConfigurationStore;

public class MarathonModule extends AbstractModule {
    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    ServiceLocator provideServiceLocator(MarathonConfig marathonConfig) {
        return new MarathonServiceLocator(marathonConfig.url());
    }

    @Provides
    @Singleton
    BrickManager provideBrickManager(MarathonConfig marathonConfig, BrickConfigurerProvider brickConfigurerProvider, ApplicationConfig applicationConfig) {
        MarathonServiceLocator marathonServiceLocator = new MarathonServiceLocator(marathonConfig.url());
        return new MarathonBrickManager(marathonConfig.url(), marathonServiceLocator, brickConfigurerProvider, applicationConfig.domain());
    }

    @Provides
    @Singleton
    ConfigurationStore provideConfigurationStore(MarathonConfig marathonConfig) {
        return new MarathonConfigurationStore(marathonConfig.url());
    }
}
