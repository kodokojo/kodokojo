package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.kodokojo.config.RedisConfig;
import io.kodokojo.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.user.UserManager;
import io.kodokojo.user.redis.RedisUserManager;

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

}
