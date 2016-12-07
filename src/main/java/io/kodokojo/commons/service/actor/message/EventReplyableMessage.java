package io.kodokojo.commons.service.actor.message;

import java.io.Serializable;

public interface EventReplyableMessage extends EventBusOriginMessage {

    String eventType();

    Serializable payloadReply();

}
