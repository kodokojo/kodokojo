package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class RabbitMqProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqProducer.class);

    private final Channel channel;

    public RabbitMqProducer(Channel channel) {
        requireNonNull(channel, "channel must be defined.");
        this.channel = channel;
    }

    public boolean publish(String exchange, List<String> messages, String routingKey, AMQP.BasicProperties props) throws Exception {
        if (isBlank(exchange)) {
            throw new IllegalArgumentException("exchange must be defined.");
        }
        requireNonNull(messages, "messages must be defined.");
        PublishCallback callback = channel -> {
            for (String m : messages) {
                channel.basicPublish(exchange, routingKey, props, m.getBytes());
            }
        };
        return publishAndAck(callback);
    }

    public boolean publish(String exchange, String message, String routingKey, AMQP.BasicProperties props) throws Exception {
        if (isBlank(exchange) && isBlank(routingKey)) {
            throw new IllegalArgumentException("exchange or routingKey must be defined.");
        }
        if (isBlank(message)) {
            throw new IllegalArgumentException("message must be defined.");
        }
        if (routingKey == null) {
            routingKey = "";
        }
        final String finalRoutingKey = routingKey;
        PublishCallback callback = channel -> {
            LOGGER.debug("Publish following message to '{}' : {}", exchange, message);
            channel.basicPublish(exchange, finalRoutingKey, props, message.getBytes());
        };
        return publishAndAck(callback);
    }

    private boolean publishAndAck(PublishCallback callback) throws Exception {
        requireNonNull(callback, "callback must be defined.");
        boolean res = false;
        synchronized (channel) {
            channel.confirmSelect();
            callback.callback(channel);
            res = channel.waitForConfirms();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Publish with confirmation a message from channel {}: {}", channel.getChannelNumber());
            }
        }
        return res;
    }

    interface PublishCallback {
        void callback(Channel channel) throws IOException;
    }

}
