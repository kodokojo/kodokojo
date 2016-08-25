package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.endpoint.SparkEndpoint;
import io.kodokojo.endpoint.UserAuthenticator;
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
    HttpEndpoint provideHttpEndpoint(ApplicationConfig applicationConfig, ApplicationLifeCycleManager applicationLifeCycleManager, UserAuthenticator<SimpleCredential> userAuthenticator, Set<SparkEndpoint> sparkEndpoints) {
        HttpEndpoint httpEndpoint = new HttpEndpoint(applicationConfig.port(), userAuthenticator, sparkEndpoints);
        applicationLifeCycleManager.addService(httpEndpoint);
        return httpEndpoint;
    }
}
