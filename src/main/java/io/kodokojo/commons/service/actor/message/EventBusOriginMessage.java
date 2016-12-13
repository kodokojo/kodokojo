package io.kodokojo.commons.service.actor.message;

import akka.actor.ActorRef;
import io.kodokojo.commons.event.Event;

public interface EventBusOriginMessage {

    Event originalEvent();

    default boolean initialSenderIsEventBus() {
        return false;
    };

    default long timeout(){
        return 60000;
    }

    default boolean requireToBeCompleteBeforeAckEventBus() {
        return true;
    }

}
