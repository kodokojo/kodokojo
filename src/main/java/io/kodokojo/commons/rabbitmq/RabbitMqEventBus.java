package io.kodokojo.commons.rabbitmq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RabbitMqEventBus implements EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqEventBus.class);

    private final RabbitMqConfig rabbitMqConfig;

    private final Object monitor = new Object();

    private final RabbitMqConnectionFactory connectionFactory;

    private final JsonToEventConverter jsonToEventConverter;

    private final EventBuilderFactory eventBuilderFactory;

    private final ThreadLocal<Gson> gson = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new GsonBuilder().create();
        }
    };
    private final MicroServiceConfig microServiceConfig;

    private Connection connection;

    private Channel channel;

    private RabbitMqEventPoller rabbitMqEventPoller;

    @Inject
    public RabbitMqEventBus(RabbitMqConfig rabbitMqConfig, MicroServiceConfig microServiceConfig,  RabbitMqConnectionFactory connectionFactory, JsonToEventConverter jsonToEventConverter, EventBuilderFactory eventBuilderFactory) {
        requireNonNull(microServiceConfig, "microServiceConfig must be defined.");
        requireNonNull(rabbitMqConfig, "rabbitMqConfig must be defined.");
        requireNonNull(connectionFactory, "connectionFactory must be defined.");
        requireNonNull(jsonToEventConverter, "eventFactory must be defined.");
        requireNonNull(eventBuilderFactory, "eventBuilderFactory must be defined.");
        this.connectionFactory = connectionFactory;
        this.microServiceConfig = microServiceConfig;
        this.rabbitMqConfig = rabbitMqConfig;
        this.jsonToEventConverter = jsonToEventConverter;
        this.eventBuilderFactory = eventBuilderFactory;
    }


    @Override
    public void connect() {

        if (channel == null) {
            synchronized (monitor) {
                if (connection == null) {
                    connection = connectionFactory.createFromRabbitMqConfig(rabbitMqConfig);
                    if (channel != null) {
                        try {
                            channel.abort();
                        } catch (IOException e) {
                            LOGGER.warn("An error occur while trying to abort a previous existing channel.", e);
                        }
                    }
                }

                try {
                    channel = connection.createChannel();
                    channel.basicQos(1);
                    channel.exchangeDeclare(rabbitMqConfig.businessExchangeName(), "fanout", true);
                    AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(rabbitMqConfig.serviceQueueName(), true, false, false, null);

                    int consumerCount = declareOk.getConsumerCount();
                    int messageCount = declareOk.getMessageCount();

                    channel.queueBind(rabbitMqConfig.serviceQueueName(), rabbitMqConfig.businessExchangeName(), "");

                    channel.exchangeDeclare(rabbitMqConfig.broadcastExchangeName(), "fanout", true);
                    channel.exchangeDeclare(microServiceConfig.name(), "fanout", false);
                    channel.exchangeBind(microServiceConfig.name(), rabbitMqConfig.broadcastExchangeName(), "");

                    String ownQueue = rabbitMqConfig.serviceQueueName() + "-" + microServiceConfig.uuid();
                    channel.queueDeclare(ownQueue, false, true, true, null);
                    channel.queueBind(ownQueue, microServiceConfig.name(), "");

                    rabbitMqEventPoller = new RabbitMqEventPoller(ownQueue, rabbitMqConfig.serviceQueueName(), channel, jsonToEventConverter);

                    channel.basicConsume(rabbitMqConfig.serviceQueueName(), false, rabbitMqEventPoller.getEventConsumer());

                    EventBuilder eventBuilder = eventBuilderFactory.create();
                    eventBuilder.setCategory(Event.Category.TECHNICAL)
                            .setEventType(Event.SERVICE_CONNECT_TYPE)
                            .setPayload(eventBuilder.getFrom());
                    broadcast(eventBuilder.build());

                    LOGGER.info("Connected to RabbitMq {}:{} on queue '{}' which contain {} message and get {} consumer(s) already connected.",
                            rabbitMqConfig.host(),
                            rabbitMqConfig.port(),
                            rabbitMqConfig.serviceQueueName(),
                            messageCount,
                            consumerCount
                    );
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to provide a valid channel or to connect to queue " + rabbitMqConfig.serviceQueueName() + ".", e);
                }
            }
        } else {
            LOGGER.warn("Connect invoked but already connected. Ignore the request.");
        }

    }

    @Override
    public EventPoller provideEventPoller() {
        return rabbitMqEventPoller;
    }

    public void broadcast(Event event) {
        requireNonNull(event, "event must be defined.");
        String message = gson.get().toJson(event);
        try {
            channel.confirmSelect();
            try {
                channel.basicPublish(rabbitMqConfig.broadcastExchangeName(), "",
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes());

            } catch (IOException e) {
                LOGGER.error("Unable to send following event :{}", message, e);
            }
            channel.waitForConfirms();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to publish following event :" + event, e);
        }
    }

    @Override
    public void send(Event event) {
        requireNonNull(event, "event must be defined.");
        try {
            channel.confirmSelect();
            publish(event);
            channel.waitForConfirms();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to publish following event :" + event, e);
        }
    }

    private void publish(Event event) {
        String message = gson.get().toJson(event);

        try {
            channel.basicPublish(rabbitMqConfig.businessExchangeName(), "",
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes());

        } catch (IOException e) {
            LOGGER.error("Unable to send following event :{}", message, e);
        }
    }

    @Override
    public void send(Set<Event> events) {
        requireNonNull(events, "events must be defined.");
        try {
            channel.confirmSelect();
            events.stream().forEach(this::publish);
            channel.waitForConfirms();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to publish following an event.", e);
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            synchronized (monitor) {
                IOUtils.closeQuietly(connection);
            }
        }
    }

}
