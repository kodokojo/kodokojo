package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.kodokojo.brick.*;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.entrypoint.UserAuthenticator;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.user.SimpleCredential;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<UserAuthenticator<SimpleCredential>>() {/**/
        }).toProvider(SimpleUserAuthenticatorProvider.class);
    }

    @Provides
    @Singleton
    ApplicationLifeCycleManager provideApplicationLifeCycleManager() {
        return new ApplicationLifeCycleManager();
    }

    @Provides
    @Singleton
    BrickStateMsgDispatcher provideBrickStateMsgDispatcher() {
        return new BrickStateMsgDispatcher();
    }

    @Provides
    @Singleton
    BrickFactory provideBrickFactory() {
        return new DefaultBrickFactory();
    }

    @Provides
    @Singleton
    BrickConfigurerProvider provideBrickConfigurerProvider(BrickUrlFactory brickUrlFactory) {
        return new DefaultBrickConfigurerProvider(brickUrlFactory);
    }

    @Provides
    @Singleton
    ProjectManager provideProjectManager(SSLKeyPair caKey, ApplicationConfig applicationConfig, BrickConfigurationStarter brickConfigurationStarter, ConfigurationStore configurationStore, ProjectStore projectStore, BootstrapConfigurationProvider bootstrapConfigurationProvider, DnsManager dnsManager, BrickUrlFactory brickUrlFactory) {
        return new DefaultProjectManager(caKey, applicationConfig.domain(), configurationStore, projectStore, bootstrapConfigurationProvider,dnsManager, brickConfigurationStarter, brickUrlFactory, applicationConfig.sslCaDuration());
    }

    @Provides
    @Singleton
    BrickUrlFactory provideBrickUrlFactory(ApplicationConfig applicationConfig) {
        return new DefaultBrickUrlFactory(applicationConfig.domain());
    }

}
