package io.kodokojo.service.actor.message;

import io.kodokojo.commons.event.Event;

import java.io.Serializable;

public interface EventReplyableMessage extends EventBusOriginMessage {

    String eventType();

    Serializable payloadReply();

}
