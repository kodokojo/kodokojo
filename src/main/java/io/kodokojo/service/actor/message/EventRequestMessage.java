/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.service.actor.message;

import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.model.User;

import java.io.Serializable;

public class EventRequestMessage implements Serializable, EventBusOriginMessage {

    protected final User requester;

    protected final Event request;

    public EventRequestMessage(User requester, Event request) {
        this.requester = requester;
        this.request = request;
    }

    public User getRequester() {
        return requester;
    }


    @Override
    public Event originalEvent() {
        return request;
    }

    @Override
    public String toString() {
        return "UserRequestMessage[" + getClass().getCanonicalName() + "]{" +
                "requester=" + requester +
                '}';
    }
}
