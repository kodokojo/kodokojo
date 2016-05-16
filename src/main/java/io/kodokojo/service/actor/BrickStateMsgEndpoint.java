package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.BrickState;
import io.kodokojo.brick.BrickStateMsgListener;

public class BrickStateMsgEndpoint extends AbstractActor {

    public BrickStateMsgEndpoint(BrickStateMsgListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        receive(ReceiveBuilder
                .match(BrickState.class, listener::receive)
                .build());
    }
}
