package io.kodokojo.commons.service.actor.message;

import io.kodokojo.commons.event.Event;

public interface EventBusOriginMessage {

    Event originalEvent();

}
