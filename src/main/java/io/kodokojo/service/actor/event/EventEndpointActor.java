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
package io.kodokojo.service.actor.event;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickStateEventDispatcher;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.project.BrickStateEventPersistenceActor;
import io.kodokojo.service.repository.ProjectRepository;

import static akka.event.Logging.getLogger;

public class EventEndpointActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(BrickStateEventDispatcher brickStateEventDispatcher, ProjectRepository projectRepository) {
        if (brickStateEventDispatcher == null) {
            throw new IllegalArgumentException("brickStateEventDispatcher must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(EventEndpointActor.class, brickStateEventDispatcher, projectRepository);
    }

    public static final String NAME = "eventEndpointProps";

    public EventEndpointActor(BrickStateEventDispatcher brickStateEventDispatcher, ProjectRepository projectRepository) {
        receive(ReceiveBuilder
                .match(EventMsg.class, msg -> {
                })
                .match(BrickStateEvent.class, msg -> {
                    LOGGER.debug("Receive  BrickStateEvent to BrickStateEventPersistenceActor.");
                    getContext().actorOf(BrickStateEventPersistenceActor.PROPS(projectRepository)).forward(msg, getContext());
                    brickStateEventDispatcher.receive(msg); // Legacy, must be removed when migrate WebsoketEntrypoint.
                })
                .matchAny(this::unhandled).build());
    }

    public static class EventMsg extends UserRequestMessage {

        private final String type;

        private final String message;

        public EventMsg(User requester, String type, String message) {
            super(requester);
            this.type = type;
            this.message = message;
        }
    }

}
