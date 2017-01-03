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

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.RabbitMqConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public interface RabbitMqConnectionFactory {

    Logger LOGGER = LoggerFactory.getLogger(RabbitMqConnectionFactory.class);

    default Connection createFromRabbitMqConfig(RabbitMqConfig config){
        LOGGER.info("Trying to connect to RabbitMq '{}:{}'.", config.host(), config.port());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.host());
        factory.setPort(config.port());

        if (StringUtils.isNotBlank(config.login())) {
            factory.setUsername(config.login());
            factory.setPassword(config.password());
        }
        try {
            //ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Unable to create a consumeConnection to Rabbit " + config.host() + ":" + config.port(), e);
        }
    }

}
