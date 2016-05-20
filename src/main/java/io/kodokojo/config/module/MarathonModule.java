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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickConfigurerProvider;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.MarathonConfig;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.marathon.MarathonBrickManager;
import io.kodokojo.service.marathon.MarathonConfigurationStore;
import io.kodokojo.service.store.ProjectStore;

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
    BrickManager provideBrickManager(MarathonConfig marathonConfig, BrickConfigurerProvider brickConfigurerProvider, ApplicationConfig applicationConfig, ProjectStore projectStore, BrickUrlFactory brickUrlFactory) {
        MarathonServiceLocator marathonServiceLocator = new MarathonServiceLocator(marathonConfig.url());
        return new MarathonBrickManager(marathonConfig.url(), marathonServiceLocator, brickConfigurerProvider, projectStore, applicationConfig.domain(), brickUrlFactory);
    }

    @Provides
    @Singleton
    ConfigurationStore provideConfigurationStore(MarathonConfig marathonConfig) {
        return new MarathonConfigurationStore(marathonConfig.url());
    }
}
