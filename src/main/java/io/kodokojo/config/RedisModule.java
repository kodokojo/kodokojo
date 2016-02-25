package io.kodokojo.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.user.UserManager;
import io.kodokojo.user.redis.RedisUserManager;

public class RedisModule extends AbstractModule {

    @Override
    protected void configure() {
        // --
    }

    @Provides
    @Singleton
    public UserManager provideUserManager(RedisConfig redisConfig) {
        return null;
    }

}
