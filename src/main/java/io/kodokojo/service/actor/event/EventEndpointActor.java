package io.kodokojo.service.actor.event;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickStateEventDispatcher;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.project.BrickStateEventPersistenceActor;
import io.kodokojo.service.repository.ProjectRepository;

public class EventEndpointActor extends AbstractActor {

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
