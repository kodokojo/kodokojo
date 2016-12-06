package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.DefaultEventBuilderFactory;
import io.kodokojo.commons.event.EventBuilderFactory;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.rabbitmq.RabbitMqConnectionFactory;
import io.kodokojo.commons.rabbitmq.RabbitMqEventBus;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class EventBusModule extends AbstractModule {

    @Override
    protected void configure() {
        // Nothing to do.
    }


    @Singleton
    @Provides
    EventBuilderFactory provideEventBuilderFactory(MicroServiceConfig microServiceConfig) {
        return new DefaultEventBuilderFactory(microServiceConfig);
    }

    @Provides
    @Singleton
    RabbitMqEventBus provideRabbitMqEventBus(RabbitMqConfig rabbitMqConfig, MicroServiceConfig microServiceConfig, RabbitMqConnectionFactory rabbitMqConnectionFactory, EventBuilderFactory eventBuilderFactory, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RabbitMqEventBus rabbitMqEventBus = new RabbitMqEventBus(rabbitMqConfig, microServiceConfig, rabbitMqConnectionFactory, new JsonToEventConverter() {
        }, eventBuilderFactory);
        applicationLifeCycleManager.addService(new ApplicationLifeCycleListener() {
            @Override
            public void start() {
                //
            }

            @Override
            public void stop() {
                rabbitMqEventBus.disconnect();
            }
        });
        rabbitMqEventBus.connect();
        return rabbitMqEventBus;
    }

    @Provides
    @Singleton
    EventBus provideEventBus(RabbitMqEventBus rabbitMqEventBus) {
        return rabbitMqEventBus;
    }

    @Provides
    @Singleton
    RabbitMqConnectionFactory proviRabbitMqConnectionFactory() {
        return new RabbitMqConnectionFactory() {

            @Override
            public Connection createFromRabbitMqConfig(RabbitMqConfig config) {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(config.host());
                factory.setPort(config.port());
                if (StringUtils.isNotBlank(config.login())) {
                    factory.setUsername(config.login());
                    factory.setPassword(config.password());
                }
                try {
                    return factory.newConnection();
                } catch (IOException | TimeoutException e) {
                    throw new RuntimeException("Unable to create a connection to Rabbit " + config.host() + ":" + config.port(), e);
                }
            }
        };
    }

}
