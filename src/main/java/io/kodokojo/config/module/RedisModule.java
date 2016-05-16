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
