package io.kodokojo.commons.event;

import java.util.Set;

public interface EventBus {

    void connect();

    EventPoller provideEventPoller();

    void send(Event event);

    void send(Set<Event> events);

    void disconnect();

}
