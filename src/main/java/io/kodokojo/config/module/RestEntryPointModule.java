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
