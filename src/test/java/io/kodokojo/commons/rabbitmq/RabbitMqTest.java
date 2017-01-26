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
package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBuilder;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.model.ServiceInfo;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitMqTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqTest.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        RabbitMqConfig rabbitMq = new RabbitMqConfig() {
            @Override
            public String host() {
                return "localhost";
            }

            @Override
            public Integer port() {
                return 5672;
            }

            @Override
            public String businessExchangeName() {
                return "business";
            }

            @Override
            public String serviceQueueName() {
                return "service";
            }

            @Override
            public String broadcastExchangeName() {
                return "broadcast";
            }

            @Override
            public String deadLetterExchangeName() {
                return "dead";
            }

            @Override
            public String deadLetterQueueName() {
                return "deadQueue";
            }

            @Override
            public String login() {
                return null;
            }

            @Override
            public String password() {
                return null;
            }

            @Override
            public Integer maxRedeliveryMessageCount() {
                return 4;
            }
        };
        String uuid = UUID.randomUUID().toString();
        MicroServiceConfig microserviceConfig = new MicroServiceConfig() {
            @Override
            public String name() {
                return "test";
            }

            @Override
            public String uuid() {
                return uuid;
            }
        };

        RabbitMqConnectionFactory rabbitMqFactory = new RabbitMqConnectionFactory() {
            @Override
            public Connection createFromRabbitMqConfig(RabbitMqConfig config) {
                LOGGER.debug("Trying to connect to {}:{}.", rabbitMq.host(), rabbitMq.port());
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(rabbitMq.host());
                factory.setPort(rabbitMq.port());
                try {
                    return factory.newConnection();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        ServiceInfo serviceinfo = new ServiceInfo(microserviceConfig.name(), microserviceConfig.uuid(), "1.0", "abcd", "testBranch");
        RabbitMqEventBus rabbitMqEventBus = new RabbitMqEventBus(rabbitMq, rabbitMqFactory, new JsonToEventConverter() {
        }, microserviceConfig, serviceinfo);

        ThreadLocal<Integer> cpt = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        EventBus.EventListener listener = event -> {

            //LOGGER.info("Receive event :\n{}", Event.convertToPrettyJson(event));
            return Try.success(Boolean.TRUE);
        };
        rabbitMqEventBus.addEventListener(listener);

        EventBuilder eventBuilder = new EventBuilder();

        eventBuilder.setFrom("tester")
                .setCategory(Event.Category.TECHNICAL)
                .setEventType(Event.SERVICE_CONNECT_TYPE)
                .setPayload(serviceinfo);
        Event event = eventBuilder.build();
        byte[] payload = Event.convertToPrettyJson(event).getBytes();
        Connection connection = rabbitMqFactory.createFromRabbitMqConfig(rabbitMq);
        Channel channel = connection.createChannel();

        long begin = System.currentTimeMillis();
        channel.confirmSelect();
        for (int i = 0; i < 500000; i++) {
            channel.basicPublish(rabbitMq.businessExchangeName(), "", null, payload);
            if (i % 50 == 0) {
                channel.waitForConfirms();
            }
        }
        long end = System.currentTimeMillis();
        long timeToPublish = end - begin;

        LOGGER.info("Queue Populate {} secondes", TimeUnit.SECONDS.convert(timeToPublish, TimeUnit.MILLISECONDS));

        channel.abort();

        rabbitMqEventBus.connect();
    }

}
