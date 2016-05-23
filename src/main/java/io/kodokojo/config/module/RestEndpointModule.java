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
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.endpoint.SparkEndpoint;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.authentification.SimpleCredential;

import java.util.Set;

public class RestEndpointModule extends AbstractModule {
    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    HttpEndpoint provideRestEntrypoint(ApplicationConfig applicationConfig, ApplicationLifeCycleManager applicationLifeCycleManager, UserAuthenticator<SimpleCredential> userAuthenticator, Set<SparkEndpoint> sparkEndpoints) {
        HttpEndpoint httpEndpoint = new HttpEndpoint(applicationConfig.port(), userAuthenticator, sparkEndpoints);
        applicationLifeCycleManager.addService(httpEndpoint);
        return httpEndpoint;
    }

}
