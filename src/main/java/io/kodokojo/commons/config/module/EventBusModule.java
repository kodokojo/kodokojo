package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.config.VersionConfig;
import io.kodokojo.commons.event.DefaultEventBuilderFactory;
import io.kodokojo.commons.event.EventBuilderFactory;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.model.ServiceInfo;
import io.kodokojo.commons.rabbitmq.RabbitMqConnectionFactory;
import io.kodokojo.commons.rabbitmq.RabbitMqEventBus;
import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleManager;
import org.apache.commons.lang.StringUtils;

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
    RabbitMqEventBus provideRabbitMqEventBus(RabbitMqConfig rabbitMqConfig, MicroServiceConfig microServiceConfig, RabbitMqConnectionFactory rabbitMqConnectionFactory, EventBuilderFactory eventBuilderFactory, ApplicationLifeCycleManager applicationLifeCycleManager, ServiceInfo serviceInfo) {

        RabbitMqEventBus rabbitMqEventBus = new RabbitMqEventBus(rabbitMqConfig, microServiceConfig, rabbitMqConnectionFactory, new JsonToEventConverter() {
        }, eventBuilderFactory, serviceInfo);
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
        return new RabbitMqConnectionFactory() {};
    }

}
