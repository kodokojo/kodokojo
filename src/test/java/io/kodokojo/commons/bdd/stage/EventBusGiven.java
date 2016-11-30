package io.kodokojo.commons.bdd.stage;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.DefaultEventBuilderFactory;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.rabbitmq.RabbitMqConnectionFactory;
import io.kodokojo.commons.rabbitmq.RabbitMqEventBus;
import io.kodokojo.commons.rabbitmq.RabbitMqEventPoller;
import io.kodokojo.commons.utils.DockerService;
import io.kodokojo.commons.utils.DockerTestApplicationBuilder;
import io.kodokojo.commons.utils.DockerTestSupport;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class EventBusGiven<SELF extends EventBusGiven<?>> extends Stage<SELF> implements DockerTestApplicationBuilder, JsonToEventConverter, RabbitMqConnectionFactory {
    @ProvidedScenarioState
    MicroServiceConfig microServiceConfig;

    @ProvidedScenarioState
    RabbitMqConfig rabbitMqConfig;

    @ProvidedScenarioState
    Connection connection;

    @ProvidedScenarioState
    RabbitMqEventPoller rabbitMqEventListener;

    @ProvidedScenarioState
    RabbitMqEventBus eventBus;

    @ProvidedScenarioState
    DockerService rabbitMq;

    public SELF a_new_event_bus_is_available(@Hidden DockerTestSupport dockerTestSupport) throws IOException, TimeoutException {
        rabbitMq = startRabbitMq(dockerTestSupport).get();
        microServiceConfig = new MicroServiceConfig() {
            @Override
            public String name() {
                return "service-a";
            }

            @Override
            public String uuid() {
                return UUID.randomUUID().toString();
            }
        };
        rabbitMqConfig = new RabbitMqConfig() {
            @Override
            public String host() {
                return rabbitMq.getHost();
            }

            @Override
            public int port() {
                return rabbitMq.getPort();
            }

            @Override
            public String businessExchangeName() {
                return "kodokojo.eventbus";
            }

            @Override
            public String serviceQueueName() {
                return "service-a";
            }

            @Override
            public String broadcastExchangeName() {
                return "kodokojo.broadcast";
            }
        };

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMq.getHost());
        connectionFactory.setPort(rabbitMq.getPort());
        connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);
        channel.exchangeDeclare(rabbitMqConfig.businessExchangeName(), "fanout", true);
        String queue = "test-listener";
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, rabbitMqConfig.businessExchangeName(), "");

        channel.exchangeDeclare(rabbitMqConfig.broadcastExchangeName(), "fanout", true);
        channel.exchangeDeclare("test-service", "fanout", false);
        channel.exchangeBind("test-service", rabbitMqConfig.broadcastExchangeName(), "");
        channel.queueBind(queue, "test-service", "");

        int consumerCount = declareOk.getConsumerCount();
        int messageCount = declareOk.getMessageCount();

        assertThat(consumerCount).isEqualTo(0);
        assertThat(messageCount).isEqualTo(0);

        rabbitMqEventListener = new RabbitMqEventPoller(queue,queue, channel, this);
        return self();
    }

    public SELF it_exit_a_fake_service() {
        eventBus = new RabbitMqEventBus(rabbitMqConfig,microServiceConfig, this, this, new DefaultEventBuilderFactory(microServiceConfig));
        return self();
    }
}