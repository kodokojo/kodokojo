package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadLetterActor extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadLetterActor.class);

    public static final Props PROPS() {
        return Props.create(DeadLetterActor.class);
    }

    public DeadLetterActor() {
        receive(ReceiveBuilder.match(DeadLetter.class, msg -> {
            LOGGER.warn("Following message fail to be distributed from {} to {} : {}", msg.sender().path(), msg.recipient().path(), msg.message().toString());
        }).build());
    }
}
