package io.kodokojo.commons.rabbitmq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;
import io.kodokojo.commons.config.MicroServiceConfig;
import io.kodokojo.commons.config.RabbitMqConfig;
import io.kodokojo.commons.event.*;
import io.kodokojo.commons.model.ServiceInfo;
import javaslang.control.Try;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RabbitMqEventBus implements EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqEventBus.class);

    public static final String UTF_8 = "UTF-8";

    protected final RabbitMqConfig rabbitMqConfig;

    protected final Object monitor = new Object();

    protected final RabbitMqConnectionFactory connectionFactory;

    protected final JsonToEventConverter jsonToEventConverter;

    protected final EventBuilderFactory eventBuilderFactory;

    protected final ThreadLocal<Gson> gson = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).create();
        }
    };
    protected final MicroServiceConfig microServiceConfig;

    protected Connection connection;

    protected Channel channel;

    private final String from;

    private final ServiceInfo serviceInfo;

    protected String localQueueName;

    protected String serviceBroadcastExhangeName;

    protected EventConsumer eventConsumer;

    protected EventConsumer localEventConsumer;

    private final Set<EventListener> waitingListeners = new HashSet<>();

    private final ReentrantLock lock;

    @Inject
    public RabbitMqEventBus(RabbitMqConfig rabbitMqConfig, MicroServiceConfig microServiceConfig, RabbitMqConnectionFactory connectionFactory, JsonToEventConverter jsonToEventConverter, EventBuilderFactory eventBuilderFactory, ServiceInfo serviceInfo) {
        requireNonNull(microServiceConfig, "microServiceConfig must be defined.");
        requireNonNull(rabbitMqConfig, "rabbitMqConfig must be defined.");
        requireNonNull(connectionFactory, "connectionFactory must be defined.");
        requireNonNull(jsonToEventConverter, "eventFactory must be defined.");
        requireNonNull(eventBuilderFactory, "eventBuilderFactory must be defined.");
        requireNonNull(serviceInfo, "serviceInfo must be defined.");
        this.connectionFactory = connectionFactory;
        this.microServiceConfig = microServiceConfig;
        this.rabbitMqConfig = rabbitMqConfig;
        this.jsonToEventConverter = jsonToEventConverter;
        this.eventBuilderFactory = eventBuilderFactory;
        this.localQueueName = rabbitMqConfig.serviceQueueName() + "-" + microServiceConfig.uuid();
        this.from = microServiceConfig.name() + "@" + microServiceConfig.uuid();
        this.serviceInfo = serviceInfo;
        this.lock = new ReentrantLock();
    }

    @Override
    public void connect() {
        connect(waitingListeners);
    }

    @Override
    public void connect(Set<EventListener> eventListeners) {
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
                    String businessQueueName = rabbitMqConfig.serviceQueueName() + "-business";
                    AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(businessQueueName, true, false, false, null);

                    int consumerCount = declareOk.getConsumerCount();
                    int messageCount = declareOk.getMessageCount();

                    channel.queueBind(businessQueueName, rabbitMqConfig.businessExchangeName(), "");

                    channel.exchangeDeclare(rabbitMqConfig.broadcastExchangeName(), "fanout", true);
                    channel.exchangeDeclare(microServiceConfig.name(), "fanout", false);
                    channel.exchangeBind(microServiceConfig.name(), rabbitMqConfig.broadcastExchangeName(), "");


                    channel.queueDeclare(localQueueName, false, true, true, null);
                    channel.queueBind(localQueueName, microServiceConfig.name(), "");

                    serviceBroadcastExhangeName = microServiceConfig.name() + "-broadcast";
                    channel.exchangeDeclare(serviceBroadcastExhangeName, "fanout", false);
                    channel.queueBind(localQueueName, serviceBroadcastExhangeName, "");

                    eventConsumer = new EventConsumer(channel, rabbitMqConfig.serviceQueueName(), eventListeners);
                    localEventConsumer = new EventConsumer(channel, localQueueName);

                    channel.basicConsume(localQueueName, false, localEventConsumer);
                    channel.basicConsume(businessQueueName, false, eventConsumer);


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
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to provide a valid channel or to connect to queue " + rabbitMqConfig.serviceQueueName() + ".", e);
                }
            }
        } else {
            LOGGER.warn("Connect invoked but already connected. Ignore the request.");
        }

    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public void broadcast(Event event) {
        requireNonNull(event, "event must be defined.");

        String message = gson.get().toJson(event);

        sendAndAck(() -> {
            try {
                channel.basicPublish(rabbitMqConfig.broadcastExchangeName(), "",
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to send following event :{}", message, e);
            }
        });
    }

    @Override
    public void broadcastToSameService(Event event) {
        requireNonNull(event, "event must be defined.");
        EventBuilder eventBuilder = new EventBuilder(event);
        eventBuilder.addCustomHeader(Event.BROADCAST_FROM_CUSTOM_HEADER, from);
        String message = gson.get().toJson(eventBuilder.build());
        sendAndAck(() -> {
            try {
                channel.basicPublish(serviceBroadcastExhangeName, "",
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to send following event :{}", message, e);
            }
        });
    }

    @Override
    public void send(Event event) {
        requireNonNull(event, "event must be defined.");
        sendAndAck(() -> publish(event));
    }

    @Override
    public Event request(Event event, int timeout, TimeUnit timeUnit) throws InterruptedException {
        requireNonNull(event, "event must be defined.");

        EventBuilder eventBuilder = new EventBuilder(event);
        eventBuilder.setReplyTo(localQueueName);
        String correlationId = UUID.randomUUID().toString();
        eventBuilder.setCorrelationId(correlationId);

        ReplyEvent replyEvent = new ReplyEvent(correlationId);
        addEventRequest(replyEvent);

        String message = gson.get().toJson(eventBuilder.build());

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .replyTo(localQueueName)
                .correlationId(correlationId)
                .deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode())
                .build();

        sendAndAck(() -> {
            try {
                channel.basicPublish(rabbitMqConfig.businessExchangeName(), "",
                        props,
                        message.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to send following event :{}", message, e);
            }
        });

        return replyEvent.getReply(timeout, timeUnit);
    }

    @Override
    public void reply(Event request, Event reply) {
        requireNonNull(request, "request must be defined.");
        requireNonNull(reply, "reply must be defined.");
        if (StringUtils.isBlank(request.getFrom())) {
            throw new IllegalArgumentException("Unable to reply to a request without from");
        }
        if (StringUtils.isBlank(request.getCorrelationId())) {
            throw new IllegalArgumentException("Unable to reply to a request without correlationId");
        }
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
        String message = gson.get().toJson(eventBuilder.build());
        sendAndAck(() -> {
            try {
                channel.basicPublish("", request.getReplyTo(),
                        props,
                        message.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to send following event :{}", message, e);
            }
        });
    }

    @Override
    public void send(Set<Event> events) {
        requireNonNull(events, "events must be defined.");
        sendAndAck(() -> events.stream().forEach(this::publish));
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            synchronized (monitor) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    LOGGER.warn("Unable close channel.", e);
                }
                IOUtils.closeQuietly(connection);
            }
        }
    }

    @Override
    public void addEventListener(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must be defined.");
        if (eventConsumer == null) {
            waitingListeners.add(eventListener);
        } else {
            this.eventConsumer.addEventlistener(eventListener);
        }
    }

    @Override
    public void removeEvenListener(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must be defined.");
        if (eventConsumer == null) {
            waitingListeners.remove(eventListener);
        } else {
            this.eventConsumer.removeEventlistener(eventListener);
        }
    }

    protected void addEventRequest(ReplyEvent event) {
        requireNonNull(event, "event must be defined.");
        if (localEventConsumer != null) {
            ConcurrentHashMap<String, ReplyEvent> requests = localEventConsumer.requests;
            long now = System.currentTimeMillis();
            List<Map.Entry<String, ReplyEvent>> toRemove = requests.entrySet().stream().filter(entry -> entry.getValue().getTimeout() < now).collect(Collectors.toList());
            toRemove.stream().forEach(entry -> {
                LOGGER.warn("Unable to get reply before timeout for following event request, removing it : {}", entry.getKey());
                requests.remove(entry.getKey());
            });
            requests.put(event.getCorrelationId(), event);
        }
    }


    class EventConsumer extends DefaultConsumer {

        protected final Set<EventListener> eventListeners = new HashSet<>();

        private final String queueName;

        private final ConcurrentHashMap<String, ReplyEvent> requests = new ConcurrentHashMap<>();

        private EventConsumer(Channel channel, String queueName, Set<EventListener> eventListeners) {
            super(channel);
            this.queueName = queueName;
            this.eventListeners.addAll(eventListeners);
        }

        private EventConsumer(Channel channel, String queueName) {
            this(channel, queueName, new HashSet<>());
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, UTF_8);
            Try.of(() -> jsonToEventConverter.converter(message))
                    .andThen(event -> {
                        if (LOGGER.isDebugEnabled() && !LOGGER.isTraceEnabled()) {
                            Gson prettyGson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
                            LOGGER.debug("EventBus receive:\n{}", prettyGson.toJson(event));
                        }

                        Iterator<EventListener> eventListenerIterator = eventListeners.iterator();
                        eventListenerIterator.forEachRemaining(eventListener -> eventListener.receive(event));

                    })
                    .andThenTry(event -> {
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }).onFailure(e -> {
                try {
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                    LOGGER.warn("Unable to process following message due to an error:\n {}", message, e);
                } catch (IOException e1) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    LOGGER.error("Unable to unack message '{}' with error: {}", message, sw.toString(), e1);
                }
            }).onSuccess(event -> {

                if (!event.getFrom().equals(from) &&
                        StringUtils.isNotBlank(event.getCorrelationId()) &&
                        requests.containsKey(event.getCorrelationId())) {
                    ReplyEvent replyEvent = requests.get(event.getCorrelationId());
                    requests.remove(event.getCorrelationId());
                    replyEvent.setReply(event);
                }
            })
                    .onFailure(e -> LOGGER.warn("Unable to process message : {}", message, e));
        }

        public void addEventlistener(EventListener eventListener) {
            eventListeners.add(eventListener);
        }

        public void removeEventlistener(EventListener eventListener) {
            eventListeners.remove(eventListener);
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

    private void sendAndAck(AckMessageSentTemplate callback) {
        lock.lock();
        try {
            channel.confirmSelect();
            callback.send();
            channel.waitForConfirms();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to send and ack an event.", e);
        } finally {
            lock.unlock();
        }
    }

    interface AckMessageSentTemplate {
        void send();
    }

}
