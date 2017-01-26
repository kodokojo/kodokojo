/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.DefaultEventBuilderFactory;
import io.kodokojo.commons.event.EventBuilderFactory;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.model.ServiceInfo;
import io.kodokojo.commons.rabbitmq.RabbitMqConnectionFactory;
import io.kodokojo.commons.rabbitmq.RabbitMqEventBus;
import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleManager;

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

        //RabbitMqEventBus rabbitMqEventBus = new RabbitMqEventBus(rabbitMqConfig, microServiceConfig, rabbitMqConnectionFactory, new JsonToEventConverter() {        }, eventBuilderFactory, serviceInfo);
        RabbitMqEventBus rabbitMqEventBus = new RabbitMqEventBus(rabbitMqConfig, rabbitMqConnectionFactory, new JsonToEventConverter() {}, microServiceConfig, serviceInfo);
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
