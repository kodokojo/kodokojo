package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface RabbitMqConfig extends PropertyConfig {

    @Key("rabbitmq.host")
    String host();

    @Key("rabbitmq.port")
    int port();

    @Key(value = "rabbitmq.business", defaultValue = "kodokojo.business")
    String businessExchangeName();

    @Key(value = "rabbitmq.service")
    String serviceQueueName();

    @Key(value = "rabbitmq.broadcast" , defaultValue = "kodokojo.broadcast")
    String broadcastExchangeName();

    @Key(value = "rabbitmq.login")
    String login();

    @Key(value = "rabbitmq.password")
    String password();

}
