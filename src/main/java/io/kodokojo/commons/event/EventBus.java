package io.kodokojo.commons.event;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface EventBus {

    void connect();

    void connect(Set<EventListener> eventListeners);

    void broadcast(Event event);

    void send(Event event);

    void send(Set<Event> events);

    List<Event> poll();

    Event request(Event request, int duration, TimeUnit timeUnit) throws InterruptedException;

    void reply(Event request, Event reply);

    void addEventListener(EventListener eventListener);

    void removeEvenListener(EventListener eventListener);

    void disconnect();

    interface EventListener {

        void receive(Event event);

    }

}
