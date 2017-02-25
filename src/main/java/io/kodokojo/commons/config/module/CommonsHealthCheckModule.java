package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.config.RedisConfig;
import io.kodokojo.commons.service.healthcheck.HealthChecker;
import io.kodokojo.commons.service.healthcheck.RabbitMqHealthChecker;
import io.kodokojo.commons.service.healthcheck.RedisHealthChecker;

public class CommonsHealthCheckModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<HealthChecker> sparkEndpointBinder = Multibinder.newSetBinder(binder(), HealthChecker.class);
        sparkEndpointBinder.addBinding().to(RedisHealthChecker.class);
        sparkEndpointBinder.addBinding().to(RabbitMqHealthChecker.class);
    }

    @Singleton
    @Provides
    RedisHealthChecker provideRedisHealthChecker(RedisConfig redisConfig) {
        return new RedisHealthChecker(redisConfig);
    }

    @Singleton
    @Provides
    RabbitMqHealthChecker provideRabbitMqHealthChecker(RabbitMqConfig rabbitMqConfig) {
        return new RabbitMqHealthChecker(rabbitMqConfig);
    }

}
