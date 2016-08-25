package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickConfigurerProvider;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.DefaultProjectManager;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.repository.ProjectRepository;

public class OrchestratorModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    ProjectManager provideProjectManager(ApplicationConfig applicationConfig, ActorSystem brickConfigurationStarter, ConfigurationStore configurationStore, ProjectRepository projectRepository, BootstrapConfigurationProvider bootstrapConfigurationProvider, DnsManager dnsManager, BrickConfigurerProvider brickConfigurerProvider, BrickUrlFactory brickUrlFactory) {
        return new DefaultProjectManager(applicationConfig.domain(), configurationStore, projectRepository, bootstrapConfigurationProvider, dnsManager, brickConfigurerProvider, brickConfigurationStarter, brickUrlFactory);
    }


}
