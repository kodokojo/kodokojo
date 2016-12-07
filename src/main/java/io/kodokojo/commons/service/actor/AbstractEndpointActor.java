package io.kodokojo.commons.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.japi.pf.UnitPFBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import io.kodokojo.commons.event.*;
import io.kodokojo.commons.service.actor.message.EventBusOriginMessage;
import io.kodokojo.commons.service.actor.message.EventReplyableMessage;
import org.apache.commons.lang.StringUtils;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public abstract class AbstractEndpointActor extends AbstractActor {

    public static final String ACTOR_PATH = "/user/endpoint";

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final String NAME = "endpointAkka";

    protected final Injector injector;

    protected final EventBus eventBus;

    public AbstractEndpointActor(Injector injector) {
        this.injector = injector;
        requireNonNull(injector, "injector must be defined.");
        EventBuilderFactory eventBuilderFactory = injector.getInstance(EventBuilderFactory.class);
        eventBus = injector.getInstance(EventBus.class);
        receive(messageMatcherBuilder()
                .match(EventReplyableMessage.class, msg -> {
                    EventBuilder eventBuilder = eventBuilderFactory.create();
                    eventBuilder.setPayload(msg.payloadReply());
                    eventBuilder.setEventType(msg.eventType());
                    eventBuilder.setCategory(Event.Category.BUSINESS);
                    Event event = eventBuilder.build();
                    eventBus.reply(msg.originalEvent(), event);
                    if (LOGGER.isDebugEnabled()) {
                        Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
                        LOGGER.debug("Reply following event to {}:\n {}", msg.originalEvent().getFrom(), gson.toJson(event));
                    }
                })
                .matchAny(this::unhandled)
                .build());
    }

    protected abstract UnitPFBuilder<Object> messageMatcherBuilder();

    protected void dispatch(Object msg, ActorRef sender, ActorRef target) {
        boolean telling = false;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Dispatch message class {} from actor {} to actor Ref {}.", msg.getClass().getCanonicalName(),sender.toString(), target.toString());
        }
        if (msg instanceof EventBusOriginMessage) {
            EventBusOriginMessage eventBusOriginMessage = (EventBusOriginMessage) msg;
            Event event = eventBusOriginMessage.originalEvent();
            if (LOGGER.isDebugEnabled()) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
                LOGGER.debug("Dispatch message contain following event :\n{}", gson.toJson(event));
            }
            if (StringUtils.isNotBlank(event.getReplyTo()) && StringUtils.isNotBlank(event.getCorrelationId())) {
                target.tell(msg, self());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("TELL following message to actor {} : {}", target.toString(), msg);
                }
                telling = true;
            }
        }
        if (!telling) {
            target.forward(msg, getContext());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("FORWARDING following message to actor {} : {}", target.toString(), msg);
            }
        }
    }

}