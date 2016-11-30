package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.RabbitMqConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public interface RabbitMqConnectionFactory {

    default Connection createFromRabbitMqConfig(RabbitMqConfig config){
        requireNonNull(config, "config must be defined.");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.host());
        factory.setPort(config.port());
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

}
