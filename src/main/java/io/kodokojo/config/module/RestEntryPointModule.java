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
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.entrypoint.RestEntryPoint;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.store.EntityStore;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.entrypoint.UserAuthenticator;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.user.SimpleCredential;

public class RestEntryPointModule extends AbstractModule {
    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    RestEntryPoint provideRestEntrypoint(ApplicationConfig applicationConfig, UserStore userStore, UserAuthenticator<SimpleCredential> userAuthenticator, EntityStore entityStore, ProjectStore projectStore, ProjectManager projectManager, BrickFactory brickFactory, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RestEntryPoint restEntryPoint = new RestEntryPoint(applicationConfig.port(), userStore, userAuthenticator, entityStore, projectStore, projectManager, brickFactory);
        applicationLifeCycleManager.addService(restEntryPoint);
        return restEntryPoint;
    }

}
