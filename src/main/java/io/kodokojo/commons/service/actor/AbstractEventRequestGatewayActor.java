package io.kodokojo.commons.service.actor;

import akka.actor.AbstractActor;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.japi.pf.UnitPFBuilder;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBuilder;
import io.kodokojo.commons.event.EventBuilderFactory;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.service.actor.message.EventBusMsg;
import io.kodokojo.commons.service.actor.message.EventBusMsg.EventBusMsgResult;
import org.apache.commons.lang.StringUtils;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public abstract class AbstractEventRequestGatewayActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    private final EventBus eventBus;

    private final EventBuilderFactory eventBuilderFactory;

    protected ActorRef originalSender;

    protected EventBusMsg initialMsg;

    public AbstractEventRequestGatewayActor(EventBus eventBus, EventBuilderFactory eventBuilderFactory) {
        requireNonNull(eventBus, "eventBus must be defined.");
        requireNonNull(eventBuilderFactory, "eventBuilderFactory must be defined.");
        this.eventBus = eventBus;
        this.eventBuilderFactory = eventBuilderFactory;
        UnitPFBuilder<Object> provideReceiverMatcher = provideReceiverMatcher();
        if (provideReceiverMatcher == null) {
            provideReceiverMatcher = ReceiveBuilder.match(EventBusMsg.class, this::sendRequestToEventBusAndWaitReply);
        } else {
            provideReceiverMatcher.match(EventBusMsg.class, this::sendRequestToEventBusAndWaitReply);
        }
        receive(provideReceiverMatcher.matchAny(this::unhandled).build());
    }

    protected UnitPFBuilder<Object> provideReceiverMatcher() {
        return ReceiveBuilder.match(EventBusMsg.class, this::sendRequestToEventBusAndWaitReply);
    }

    protected void receiveReply(EventBusMsgResult eventBusMsgResult) {
        originalSender.tell(eventBusMsgResult, self());
    }

    protected void postReply() {
        //  Nothing to do.
    }

    private void sendRequestToEventBusAndWaitReply(EventBusMsg msg) {
        initialMsg = msg;
        originalSender = sender();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Receive a request [{}] which will process via EventBus from actor {}.", msg.getClass().getCanonicalName(), originalSender);
        }
        Event event = null;
        if (StringUtils.isNotBlank(msg.eventType()) && msg.payload() != null) {
            EventBuilder eventBuilder = eventBuilderFactory.create();
            eventBuilder.setEventType(msg.eventType())
                    .setPayload(msg.payload());
            event = eventBuilder.build();
        } else {
            event = msg.provideEvent(eventBuilderFactory.create());
        }

        if (event == null) {
            throw new IllegalArgumentException("Not able to build an Event from message: " + msg);
        }
        EventBusMsgResult eventBusMsgResult = null;
        try {
            Event reply = eventBus.request(event, msg.duration(), msg.timeunit());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Receive reply Event :\n{}", Event.convertToPrettyJson(reply));
            }
            eventBusMsgResult = new EventBusMsgResult(msg, reply, false);
        } catch (InterruptedException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Timeout excess for message {}: {}", msg, e);
            }
            eventBusMsgResult = new EventBusMsgResult(msg, null, true);
        } finally {
            receiveReply(eventBusMsgResult);
            postReply();
            getContext().stop(self());
        }
    }


}
