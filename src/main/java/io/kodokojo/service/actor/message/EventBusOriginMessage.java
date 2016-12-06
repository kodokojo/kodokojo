package io.kodokojo.service.actor.message;

import io.kodokojo.commons.event.Event;

public interface EventBusOriginMessage {

    Event originalEvent();

}
