package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.RabbitMqConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Unable to create a connection to Rabbit " + config.host() + ":" + config.port(), e);
        }
    }

}
