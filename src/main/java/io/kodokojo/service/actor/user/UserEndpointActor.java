package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.UserRepository;

public class UserEndpointActor extends AbstractActor {

    public static Props PROPS(UserRepository userRepository) {
        return Props.create(UserEndpointActor.class, userRepository);
    }

    private final UserRepository userRepository;

    public UserEndpointActor(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        this.userRepository = userRepository;

        receive(ReceiveBuilder.match(UserCreatorActor.UserCreateMsg.class, msg -> {
            getContext().actorOf(UserCreatorActor.PROPS(userRepository)).tell(msg, self());
        })
            .match(UserCreatorActor.UserCreatedMessage.class, msg -> {
                getContext().parent().tell(msg, sender());
            })
        .matchAny(this::unhandled)
        .build());

    }

}
