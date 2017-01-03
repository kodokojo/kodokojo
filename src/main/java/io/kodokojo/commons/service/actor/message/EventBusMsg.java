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
package io.kodokojo.commons.service.actor.message;


import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBuilder;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public interface EventBusMsg {

    String eventType();

    Serializable payload();

    default Event provideEvent(EventBuilder eventBuilder) {
        return null;
    }

    default TimeUnit timeunit() {
        return TimeUnit.MINUTES;
    }

    default int duration() {
        return 1;
    }

    default String requestIdentifier() {
        return null;
    };

    public class EventBusMsgResult {

        private final EventBusMsg originalEventBusMsg;

        private final Event reply;

        private final boolean timeoutExess;

        public EventBusMsgResult(EventBusMsg originalEventBusMsg, Event reply, boolean timeoutExess) {
            requireNonNull(originalEventBusMsg, "originalEventBusMsg must be defined.");
            this.originalEventBusMsg = originalEventBusMsg;
            this.reply = reply;
            this.timeoutExess = timeoutExess;
        }

        public EventBusMsg getOriginalEventBusMsg() {
            return originalEventBusMsg;
        }

        public Event getReply() {
            return reply;
        }

        public boolean isTimeoutExess() {
            return timeoutExess;
        }
    }
}
