package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.BrickStateMsg;
import io.kodokojo.service.BrickStateMsgListener;

public class BrickStateMsgEndpoint extends AbstractActor {

    public BrickStateMsgEndpoint(BrickStateMsgListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        receive(ReceiveBuilder
                .match(BrickStateMsg.class, listener::receive)
                .build());
    }
}
