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
import io.kodokojo.config.ZookeeperConfig;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.zookeeper.ZookeeperBootstrapConfigurationProvider;
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

    @Provides
    @Singleton
    BootstrapConfigurationProvider provideBootstrapConfigurationProvider(ZookeeperConfig zookeeperConfig) {
        return new ZookeeperBootstrapConfigurationProvider(zookeeperConfig);
    }
}
