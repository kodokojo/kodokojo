package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.PropertyConfig;

public interface RabbitMqConfig extends PropertyConfig {

    String host();

    int port();

    String businessExchangeName();

    String serviceQueueName();

    String broadcastExchangeName();

}
