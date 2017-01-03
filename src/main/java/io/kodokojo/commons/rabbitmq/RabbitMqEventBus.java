package io.kodokojo.commons.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.*;
import io.kodokojo.commons.model.ServiceInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RabbitMqEventBus implements EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqEventBus.class);

    private static final String UTF_8 = "UTF-8";

    private static final String FANOUT = "fanout";

    private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    private final RabbitMqConfig rabbitMqConfig;

    private final Object monitor = new Object();

    private final RabbitMqConnectionFactory connectionFactory;

    private final JsonToEventConverter jsonToEventConverter;

    protected final EventBuilderFactory eventBuilderFactory;

    private final MicroServiceConfig microServiceConfig;

    private final String from;

    private final String businessQueueName;

    private final String localQueueName;

    private final String serviceBroadcastExhangeName;

    private final Set<EventListener> waitingListeners = new HashSet<>();

    private final Map<String, ReplyEvent> requests = new ConcurrentHashMap<>();

    private final ServiceInfo serviceInfo;

    protected Connection connection;

    private RabbitMqConsumer consumer;

    private RabbitMqProducer producer;


    public RabbitMqEventBus(RabbitMqConfig rabbitMqConfig, RabbitMqConnectionFactory connectionFactory, JsonToEventConverter jsonToEventConverter, MicroServiceConfig microServiceConfig, ServiceInfo serviceInfo) {
        requireNonNull(rabbitMqConfig, "rabbitMqConfig must be defined.");
        requireNonNull(connectionFactory, "connectionFactory must be defined.");
        requireNonNull(jsonToEventConverter, "jsonToEventConverter must be defined.");
        requireNonNull(microServiceConfig, "microServiceConfig must be defined.");
        requireNonNull(serviceInfo, "serviceInfo must be defined.");
        eventBuilderFactory = new DefaultEventBuilderFactory(microServiceConfig);

        this.rabbitMqConfig = rabbitMqConfig;
        this.connectionFactory = connectionFactory;
        this.jsonToEventConverter = jsonToEventConverter;
        this.microServiceConfig = microServiceConfig;
        this.serviceInfo = serviceInfo;
        this.businessQueueName = rabbitMqConfig.serviceQueueName() + "-business";
        this.localQueueName = rabbitMqConfig.serviceQueueName() + "-" + microServiceConfig.uuid();
        this.serviceBroadcastExhangeName = microServiceConfig.name() + "-broadcast";
        this.from = microServiceConfig.name() + "@" + microServiceConfig.uuid();
    }

    @Override
    public void connect() {
        connect(waitingListeners);
    }

    @Override
    public void connect(Set<EventListener> eventListeners) {
        requireNonNull(eventListeners, "eventListeners must be defined.");
        if (!isConnected()) {
            synchronized (monitor) {
                if (!isConnected()) {
                    try {
                        initConnection();
                    } catch (IOException e) {
                        LOGGER.debug("Unable to initiate connection.", e);
                        if (connection != null) {
                            IOUtils.closeQuietly(connection);
                            connection = null;
                        }
                    }
                }
            }
        }
    }

    public boolean isConnected() {
        synchronized (monitor) {
            return connection != null && connection.isOpen();
        }
    }

    private void initConnection() throws IOException {

        connection = connectionFactory.createFromRabbitMqConfig(rabbitMqConfig);

        //  Configure default Exchange, service queue, etc...
        Channel channel = connection.createChannel();
        Channel localChannel = connection.createChannel();
        Channel publishChannel = connection.createChannel();

        channel.exchangeDeclare(rabbitMqConfig.deadLetterExchangeName(), FANOUT, true, false, null);
        channel.queueDeclare(rabbitMqConfig.deadLetterQueueName(), true, false, false, null);
        channel.queueBind(rabbitMqConfig.deadLetterQueueName(), rabbitMqConfig.deadLetterExchangeName(), "");

        Map<String, Object> args = new HashMap<>();
        args.put(X_DEAD_LETTER_EXCHANGE, rabbitMqConfig.deadLetterExchangeName());


        channel.exchangeDeclare(rabbitMqConfig.businessExchangeName(), FANOUT, true, false, args);
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(businessQueueName, true, false, false, args);

        int consumerCount = declareOk.getConsumerCount();
        int messageCount = declareOk.getMessageCount();

        channel.queueBind(businessQueueName, rabbitMqConfig.businessExchangeName(), "");

        channel.exchangeDeclare(rabbitMqConfig.broadcastExchangeName(), FANOUT, true, false, args);
        channel.exchangeDeclare(microServiceConfig.name(), FANOUT, false, false, args);
        channel.exchangeBind(microServiceConfig.name(), rabbitMqConfig.broadcastExchangeName(), "");


        channel.queueDeclare(localQueueName, false, true, false, args);
        channel.queueBind(localQueueName, microServiceConfig.name(), "");

        channel.exchangeDeclare(serviceBroadcastExhangeName, FANOUT, false, false, args);
        channel.queueBind(localQueueName, serviceBroadcastExhangeName, "");


        RabbitMqConsumer.RabbitMqListener listener = (channelInner, consumerTag, envelope, properties, payload) -> {
            Event event = jsonToEventConverter.converter(payload);
            if (from.equals(event.getFrom())) {
                LOGGER.debug("We are sender, ignore this message.");
            } else {
                String correlationId = event.getCorrelationId();
                if (StringUtils.isNotBlank(correlationId)) {
                    if (requests.containsKey(correlationId)) {
                        requests.get(correlationId).setReply(event);
                        requests.remove(correlationId);
                        LOGGER.debug("Receive and remove request for following reply to request with correlation ID : {}\n{}", correlationId, Event.convertToPrettyJson(event));
                    } else {
                        LOGGER.debug("Receive a Reply form a Request we don't request [correlationId:{}]:\n{}", correlationId, Event.convertToPrettyJson(event));
                    }
                }
                Map<String, String> custom = event.getCustom();
                if (custom.containsKey(Event.BROADCAST_FROM_CUSTOM_HEADER) &&
                        from.equals(custom.get(Event.BROADCAST_FROM_CUSTOM_HEADER))) {
                    LOGGER.debug("Ignore a broacasted message sent by us.");
                } else {
                    for (EventListener eventListener : waitingListeners) {
                        eventListener.receive(event);
                    }
                }

            }
        };

        Set<String> queues = new HashSet<>();
        queues.add(localQueueName);
        consumer = new RabbitMqConsumer(localChannel, queues, listener);
        queues = new HashSet<>();
        queues.add(businessQueueName);
        consumer = new RabbitMqConsumer(channel, queues, listener);
        producer = new RabbitMqProducer(publishChannel);


        EventBuilder eventBuilder = eventBuilderFactory.create();
        eventBuilder.setCategory(Event.Category.TECHNICAL)
                .setEventType(Event.SERVICE_CONNECT_TYPE)
                .setPayload(serviceInfo);
        broadcast(eventBuilder.build());

        LOGGER.info("Connected to RabbitMq {}:{} on queue '{}' which contain {} message and get {} consumer(s) already connected.",
                rabbitMqConfig.host(),
                rabbitMqConfig.port(),
                rabbitMqConfig.serviceQueueName(),
                messageCount,
                consumerCount
        );
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public void broadcast(Event event) {
        requireNonNull(event, "event must be defined.");
        connect();
        String message = Event.convertToJson(event);
        try {
            producer.publish(rabbitMqConfig.broadcastExchangeName(), message, null, null);
        } catch (Exception e) {
            LOGGER.error("An error occur while broadcasting following event:\n{}", Event.convertToPrettyJson(event), e);
        }
    }

    @Override
    public void broadcastToSameService(Event event) {
        requireNonNull(event, "event must be defined.");
        connect();

        EventBuilder eventBuilder = new EventBuilder(event);
        eventBuilder.addCustomHeader(Event.BROADCAST_FROM_CUSTOM_HEADER, from);
        Event eventToSend = eventBuilder.build();
        String message = Event.convertToJson(eventToSend);
        try {
            producer.publish(serviceBroadcastExhangeName, message, null, null);
        } catch (Exception e) {
            LOGGER.error("An error occur while broadcasting following event:\n{}", Event.convertToPrettyJson(eventToSend), e);
        }
    }

    @Override
    public void send(Event event) {
        requireNonNull(event, "event must be defined.");
        connect();
        String message = Event.convertToJson(event);
        try {
            producer.publish(rabbitMqConfig.businessExchangeName(), message, null, null);
        } catch (Exception e) {
            LOGGER.error("Unable to send event:\n{}", Event.convertToPrettyJson(event), e);
        }
    }

    @Override
    public void send(Set<Event> events) {
        requireNonNull(events, "events must be defined.");
        connect();
        List<String> messages = events.stream().map(Event::convertToJson).collect(Collectors.toList());
        try {
            producer.publish(rabbitMqConfig.businessExchangeName(), messages, null, null);
        } catch (Exception e) {
            LOGGER.error("Unable to send a list of events.", e);
        }
    }

    @Override
    public Event request(Event request, int duration, TimeUnit timeUnit) throws InterruptedException {
        requireNonNull(request, "event must be defined.");
        connect();

        EventBuilder eventBuilder = new EventBuilder(request);
        eventBuilder.setReplyTo(localQueueName);
        String correlationId = UUID.randomUUID().toString();
        eventBuilder.setCorrelationId(correlationId);

        ReplyEvent replyEvent = new ReplyEvent(correlationId);

        addEventRequest(replyEvent);

        String message = Event.convertToJson(eventBuilder.build());

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .replyTo(localQueueName)
                .correlationId(correlationId)
                .deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode())
                .build();

        try {
            producer.publish(rabbitMqConfig.businessExchangeName(), message, null, props);
        } catch (Exception e) {
            LOGGER.error("Unable to publish request with correlationId {}.", correlationId, e);
        }

        return replyEvent.getReply(duration, timeUnit);
    }

    @Override
    public void reply(Event request, Event reply) {
        requireNonNull(request, "request must be defined.");
        requireNonNull(reply, "reply must be defined.");
        if (StringUtils.isBlank(request.getFrom())) {
            throw new IllegalArgumentException("Unable to reply to a request without from");
        }
        if (reply.getFrom().equals(from)) {
            throw new IllegalArgumentException("Unable to reply to myself.");
        }
        if (StringUtils.isBlank(request.getCorrelationId())) {
            throw new IllegalArgumentException("Unable to reply to a request without correlationId");
        }

        connect();
        EventBuilder eventBuilder = eventBuilderFactory.create();
        eventBuilder.setEvent(reply);
        if (StringUtils.isBlank(reply.getCorrelationId())) {
            eventBuilder.setCorrelationId(request.getCorrelationId());
        } else if (!request.getCorrelationId().equals(reply.getCorrelationId())) {
            throw new IllegalArgumentException("Request correlationId " + request.getCorrelationId() + " is different in reply [" + reply.getCorrelationId() + "].");
        }

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(request.getCorrelationId())
                .deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode())
                .build();
        Event event = eventBuilder.build();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending following Reply to {}:\n{}", request.getReplyTo(), Event.convertToPrettyJson(event));
        }
        String message = Event.convertToJson(event);
        try {
            producer.publish("", message, request.getReplyTo(), props);
        } catch (Exception e) {
            LOGGER.error("Unable to publish reply with correletaionId {} to {}", event.getCorrelationId(), request.getReplyTo(), e);
        }

    }

    @Override
    public void addEventListener(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must be defined.");
        waitingListeners.add(eventListener);
    }

    @Override
    public void removeEvenListener(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must be defined.");
        waitingListeners.remove(eventListener);
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            synchronized (monitor) {
                IOUtils.closeQuietly(connection);
            }
        }
    }


    private void addEventRequest(ReplyEvent event) {
        requireNonNull(event, "event must be defined.");
        connect();
        long now = System.currentTimeMillis();
        List<Map.Entry<String, ReplyEvent>> toRemove = requests.entrySet().stream().filter(entry -> entry.getValue().getTimeout() < now).collect(Collectors.toList());
        toRemove.stream().forEach(entry -> {
            LOGGER.warn("Unable to get reply before timeout for following event request, removing it : {}", entry.getKey());
            requests.remove(entry.getKey());
        });
        requests.put(event.getCorrelationId(), event);
        LOGGER.debug("Correlation Request added :{}", event.getCorrelationId());
    }
}
