package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventPoller;
import io.kodokojo.commons.event.JsonToEventConverter;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.Objects.requireNonNull;

public class RabbitMqEventPoller implements EventPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqEventPoller.class);

    public static final String UTF_8 = "UTF-8";

    private final JsonToEventConverter jsonToEventConverter;

    private final Object monitor = new Object();

    private final String localQueueName;

    private final String serviceQueueName;

    private Channel channel;

    private EventConsumer eventConsumer;

    @Inject
    public RabbitMqEventPoller(String localQueueName, String serviceQueueName, Channel channel, JsonToEventConverter jsonToEventConverter) {
        this.localQueueName = localQueueName;
        this.serviceQueueName = serviceQueueName;

        requireNonNull(channel, "channel must be defined.");
        requireNonNull(jsonToEventConverter, "eventFactory must be defined.");
        this.channel = channel;
        this.jsonToEventConverter = jsonToEventConverter;
        provideEventConsumer();
    }


    @Override
    public List<Event> poll() {
        provideEventConsumer();
        if (eventConsumer == null) {
            throw new IllegalStateException("consumer is not expected to be null.");
        }
        return eventConsumer.pollEvents();
    }

    protected EventConsumer getEventConsumer() {
        provideEventConsumer();
        return eventConsumer;
    }

    private void provideEventConsumer() {
        if (eventConsumer == null) {
            synchronized (monitor) {
                if (eventConsumer == null) {
                    eventConsumer = new EventConsumer(channel);
                    try {
                        channel.basicConsume(localQueueName, false, eventConsumer);
                        channel.basicConsume(serviceQueueName, false, eventConsumer);
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to bind rabbitmq consumer to channel", e);
                    }
                }
            }
        }
    }


    class EventConsumer extends DefaultConsumer {

        private final BlockingDeque<Event> queues = new LinkedBlockingDeque<>();

        public EventConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, UTF_8);
            Try.of(() -> jsonToEventConverter.converter(message))
                    .andThen(queues::add)
                    .andThenTry(event -> {
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }).onSuccess(event -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Following Event add to queue : {}", event);
                }
            }).onFailure(e -> LOGGER.warn("Unable to process message : {}", message, e));
        }

        public List<Event> pollEvents() {
            List<Event> res = new ArrayList<>();
            queues.drainTo(res);
            return res;
        }

    }

}
