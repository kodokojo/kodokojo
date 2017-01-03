/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
