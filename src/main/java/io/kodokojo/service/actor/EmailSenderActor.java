package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.EmailSender;

public class EmailSenderActor extends AbstractActor {

    public static Props PROPS(EmailSender emailSender) {
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        return Props.create(EmailSenderActor.class, emailSender);
    }

    private final EmailSender emailSender;

    public EmailSenderActor(EmailSender emailSender) {
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        this.emailSender = emailSender;
        receive(ReceiveBuilder.match(EmailSenderMessage.class, msg -> {

        }).matchAny(this::unhandled).build());
    }

    public static class EmailSenderMessage {

    }

}
