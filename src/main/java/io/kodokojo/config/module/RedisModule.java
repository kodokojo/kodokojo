package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.RedisConfig;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.ProjectStore;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.UserManager;
import io.kodokojo.service.redis.RedisBootstrapConfigurationProvider;
import io.kodokojo.service.redis.RedisProjectStore;
import io.kodokojo.service.redis.RedisUserManager;

import javax.crypto.SecretKey;
import javax.inject.Named;

public class RedisModule extends AbstractModule {

    @Override
    protected void configure() {
        /*
        Multibinder<UserManager> multibinder = Multibinder.newSetBinder(binder(), UserManager.class);
        multibinder.addBinding().toProvider(RedisUserManagerProvider.class);
        */
    }

    @Provides
    @Singleton
    UserManager provideRediUserManager(@Named("securityKey")SecretKey secretKey, RedisConfig redisConfig, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisUserManager redisUserManager = new RedisUserManager(secretKey, redisConfig.host(), redisConfig.port());
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
    ProjectStore provideProjectStore(@Named("securityKey") SecretKey key, RedisConfig redisConfig, BrickFactory brickFactory, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisProjectStore redisProjectStore = new RedisProjectStore(key, redisConfig.host(), redisConfig.port(), brickFactory);
        applicationLifeCycleManager.addService(redisProjectStore);
        return redisProjectStore;
    }

}
