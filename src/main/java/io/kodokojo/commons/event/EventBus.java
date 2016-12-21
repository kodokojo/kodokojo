package io.kodokojo.commons.event;

import javaslang.control.Try;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface EventBus {

    void connect();

    void connect(Set<EventListener> eventListeners);

    String getFrom();

    void broadcast(Event event);

    void broadcastToSameService(Event event);

    void send(Event event);

    void send(Set<Event> events);

    Event request(Event request, int duration, TimeUnit timeUnit) throws InterruptedException;

    void reply(Event request, Event reply);

    void addEventListener(EventListener eventListener);

    void removeEvenListener(EventListener eventListener);

    void disconnect();

    interface EventListener {

        Try<Boolean> receive(Event event);

    }

}
