package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.RedisConfig;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.redis.RedisEntityStore;
import io.kodokojo.service.redis.RedisUserStore;
import io.kodokojo.service.store.EntityStore;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.redis.RedisBootstrapConfigurationProvider;
import io.kodokojo.service.redis.RedisProjectStore;

import javax.crypto.SecretKey;
import javax.inject.Named;

public class RedisModule extends AbstractModule {

    @Override
    protected void configure() {
        /*
        Multibinder<UserStore> multibinder = Multibinder.newSetBinder(binder(), UserStore.class);
        multibinder.addBinding().toProvider(RedisUserManagerProvider.class);
        */
    }

    @Provides
    @Singleton
    UserStore provideRediUserManager(@Named("securityKey")SecretKey secretKey, RedisConfig redisConfig, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisUserStore redisUserManager = new RedisUserStore(secretKey, redisConfig.host(), redisConfig.port());
        applicationLifeCycleManager.addService(redisUserManager);
        return redisUserManager;
    }

    @Provides
    @Singleton
    BootstrapConfigurationProvider provideBootstrapConfigurationProvider(ApplicationConfig applicationConfig, RedisConfig redisConfig, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisBootstrapConfigurationProvider redisBootstrapConfigurationProvider = new RedisBootstrapConfigurationProvider(redisConfig.host(), redisConfig.port(), applicationConfig.defaultLoadbalancerIp(), applicationConfig.initialSshPort());
        applicationLifeCycleManager.addService(redisBootstrapConfigurationProvider);
        return redisBootstrapConfigurationProvider;
    }

    @Provides
    @Singleton
    EntityStore provideEntityStore(@Named("securityKey") SecretKey key, RedisConfig redisConfig, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisEntityStore entityStore = new RedisEntityStore(key, redisConfig.host(), redisConfig.port());
        applicationLifeCycleManager.addService(entityStore);
        return entityStore;
    }

    @Provides
    @Singleton
    ProjectStore provideProjectStore(@Named("securityKey") SecretKey key, RedisConfig redisConfig, BrickFactory brickFactory, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisProjectStore redisProjectStore = new RedisProjectStore(key, redisConfig.host(), redisConfig.port(), brickFactory);
        applicationLifeCycleManager.addService(redisProjectStore);
        return redisProjectStore;
    }

}
