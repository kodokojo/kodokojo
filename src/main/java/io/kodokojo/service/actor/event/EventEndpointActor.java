package io.kodokojo.service.actor.event;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;

import java.util.Set;

public class EventEndpointActor extends AbstractActor {

    public static Props PROPS() {
        return Props.create(EventEndpointActor.class);
    }

    public EventEndpointActor() {
        receive(ReceiveBuilder.match(EventMsg.class, msg -> {}).matchAny(this::unhandled).build());
    }

    public static class EventMsg extends UserRequestMessage {

        private final String type;

        private final String message;

        public EventMsg(User requester, String type, String message) {
            super(requester);
            this.type = type;
            this.message = message;
        }
    }

}
