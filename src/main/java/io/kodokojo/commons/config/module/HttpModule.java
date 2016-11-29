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
package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.ApplicationConfig;
import io.kodokojo.commons.config.VersionConfig;
import io.kodokojo.api.endpoint.HttpEndpoint;
import io.kodokojo.commons.endpoint.SparkEndpoint;
import io.kodokojo.commons.endpoint.UserAuthenticator;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;

import java.util.Set;

public class HttpModule extends AbstractModule {

    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    HttpEndpoint provideHttpEndpoint(ApplicationConfig applicationConfig, ApplicationLifeCycleManager applicationLifeCycleManager, UserAuthenticator<SimpleCredential> userAuthenticator, Set<SparkEndpoint> sparkEndpoints, VersionConfig versionConfig) {
        HttpEndpoint httpEndpoint = new HttpEndpoint(applicationConfig.port(), userAuthenticator, sparkEndpoints, versionConfig);
        applicationLifeCycleManager.addService(httpEndpoint);
        return httpEndpoint;
    }
}
