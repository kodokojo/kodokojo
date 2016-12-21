package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface RabbitMqConfig extends PropertyConfig {

    String RABBITMQ_HOST = "rabbitmq.host";

    String RABBITMQ_PORT = "rabbitmq.port";

    @Key(value = RABBITMQ_HOST, defaultValue = "rabbitmq")
    String host();

    @Key(value = RABBITMQ_PORT, defaultValue = "5672")
    Integer port();

    @Key(value = "rabbitmq.business", defaultValue = "kodokojo.business")
    String businessExchangeName();

    @Key(value = "rabbitmq.service")
    String serviceQueueName();

    @Key(value = "rabbitmq.broadcast", defaultValue = "kodokojo.broadcast")
    String broadcastExchangeName();

    @Key(value = "rabbitmq.deadletter", defaultValue = "kodokojo.deadletter")
    String deadLetterExchangeName();

    @Key(value = "rabbitmq.deadletter.queue", defaultValue = "kodokojo.deadletterQueue")
    String deadLetterQueueName();

    @Key(value = "rabbitmq.login")
    String login();

    @Key(value = "rabbitmq.password")
    String password();

    @Key(value = "rabbitmq.maxRedeliveryMessageCount", defaultValue = "3")
    Integer maxRedeliveryMessageCount();


}
