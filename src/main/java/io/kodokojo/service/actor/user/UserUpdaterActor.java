package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.UserRepository;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public class UserUpdaterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    private final UserRepository userRepository;

    public UserUpdaterActor(UserRepository userRepository) {
        this.userRepository = userRepository;

        receive(ReceiveBuilder
                .match(UserMessage.UserUpdateMessage.class, this::onUserUpdate)
                .matchAny(this::unhandled)
                .build()
        );
    }

    static Props PROPS(UserRepository userRepository) {
        requireNonNull(userRepository, "userRepository must be defined.");
        return Props.create(UserUpdaterActor.class, userRepository);
    }

    private void onUserUpdate(UserMessage.UserUpdateMessage msg) {
        userRepository.updateUser(msg.getUserToUpdate());
        sender().tell(new UserMessage.UserUpdateMessageResult(msg.getRequester(), true), self());
        getContext().stop(self());
    }

}
