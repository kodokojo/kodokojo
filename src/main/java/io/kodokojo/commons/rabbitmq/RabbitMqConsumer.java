package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RabbitMqConsumer implements Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqConsumer.class);

    private static final String UTF_8 = "UTF-8";

    private final RabbitMqListener listener;

    private final Channel channel;

    public interface RabbitMqListener {

        void receive(Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, String payloa);

    }

    public RabbitMqConsumer(Channel channel, Set<String> queues, RabbitMqListener listener) {
        requireNonNull(channel, "channel must be defined.");
        requireNonNull(queues, "queues must be defined.");
        requireNonNull(listener, "listener must be defined.");
        this.listener = listener;
        this.channel = channel;
        for (String queueName : queues) {
            try {
                channel.basicConsume(queueName, false, this);
                channel.basicQos(50);
            } catch (IOException e) {
                LOGGER.error("Unable to register consumer on following queue ''.", queueName, e);
            }
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, UTF_8);
        //synchronized (channel) {
        try {
            listener.receive(channel, consumerTag, envelope, properties, message);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ack message with delivery tag '{}' from exchange '{}' on channel {}", envelope.getDeliveryTag(), envelope.getExchange(), channel.getChannelNumber());
            }
            channel.basicAck(envelope.getDeliveryTag(), false);
        } catch (RuntimeException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Following exception occur while process message with tag '{}', NACK this message.", envelope.getDeliveryTag(), e);
            }
            channel.basicNack(envelope.getDeliveryTag(), false, false);
        }
        //}

    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        //  Nothing to do.
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        //  Nothing to do.
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        //  Nothing to do.
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        //  Nothing to do.
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        //  Nothing to do.
    }
}
