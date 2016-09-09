/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
