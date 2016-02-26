package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.entrypoint.RestEntrypoint;
import io.kodokojo.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.user.SimpleCredential;
import io.kodokojo.user.UserAuthenticator;
import io.kodokojo.user.UserManager;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<UserAuthenticator<SimpleCredential>>() {/**/}).toProvider(SimpleUserAuthenticatorProvider.class);
    }

    @Provides
    @Singleton
    ApplicationLifeCycleManager provideApplicationLifeCycleManager() {
        return new ApplicationLifeCycleManager();
    }

    @Provides
    @Singleton
    RestEntrypoint provideRestEntrypoint(ApplicationConfig applicationConfig, UserManager userManager, UserAuthenticator<SimpleCredential> userAuthenticator, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RestEntrypoint restEntrypoint = new RestEntrypoint(applicationConfig.port(), userManager, userAuthenticator);
        applicationLifeCycleManager.addService(restEntrypoint);
        return restEntrypoint;
    }

}
