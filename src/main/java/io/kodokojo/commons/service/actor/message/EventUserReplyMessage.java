/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.service.actor.message;

import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.model.User;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class EventUserReplyMessage extends EventUserRequestMessage implements EventReplyableMessage{

    private final String eventType;

    private final Serializable payloadReply;

    public EventUserReplyMessage(User requester, Event request, String eventType, Serializable payloadReply) {
        super(requester, request);
        requireNonNull(payloadReply, "payloadReply must be defined.");
        if (isBlank(eventType)) {
            throw new IllegalArgumentException("eventType must be defined.");
        }
        this.eventType = eventType;
        this.payloadReply = payloadReply;
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public Serializable payloadReply() {
        return payloadReply;
    }
}
