package io.kodokojo.commons.service.actor.message;


import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBuilder;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public interface EventBusMsg {

    String eventType();

    Serializable payload();

    default Event provideEvent(EventBuilder eventBuilder) {
        return null;
    }

    default TimeUnit timeunit() {
        return TimeUnit.MINUTES;
    }

    default int duration() {
        return 1;
    }

    default String requestIdentifier() {
        return null;
    };

    public class EventBusMsgResult {

        private final EventBusMsg originalEventBusMsg;

        private final Event reply;

        private final boolean timeoutExess;

        public EventBusMsgResult(EventBusMsg originalEventBusMsg, Event reply, boolean timeoutExess) {
            requireNonNull(originalEventBusMsg, "originalEventBusMsg must be defined.");
            this.originalEventBusMsg = originalEventBusMsg;
            this.reply = reply;
            this.timeoutExess = timeoutExess;
        }

        public EventBusMsg getOriginalEventBusMsg() {
            return originalEventBusMsg;
        }

        public Event getReply() {
            return reply;
        }

        public boolean isTimeoutExess() {
            return timeoutExess;
        }
    }
}
