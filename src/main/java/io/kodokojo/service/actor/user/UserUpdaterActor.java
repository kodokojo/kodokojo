package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.UpdateData;
import io.kodokojo.model.User;
import io.kodokojo.model.UserBuilder;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.project.ProjectUpdaterMessages;
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
        User oldUser = userRepository.getUserByIdentifier(msg.getUserToUpdate().getIdentifier());
        UserBuilder builder = new UserBuilder(msg.getUserToUpdate());
        builder.setPassword(msg.getNewPassword());
        builder.setSshPublicKey(msg.getNewSSHPublicKey());
        User user = builder.build();
        userRepository.updateUser(user);
        sender().tell(new UserMessage.UserUpdateMessageResult(msg.getRequester(), true), self());
        getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg(msg.getRequester(), new UpdateData<>(oldUser, user)), self());
        getContext().stop(self());
    }

}
