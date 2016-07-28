package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.UserRepository;

public class UserEligibleActor extends AbstractActor {

    public static Props PROPS(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        return Props.create(UserEligibleActor.class, userRepository);
    }

    public UserEligibleActor(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        receive(ReceiveBuilder.match(UserCreatorActor.UserCreateMsg.class , msg -> {
            boolean res = userRepository.identifierExpectedNewUser(msg.id);
            if (res) {
                res = userRepository.getUserByUsername(msg.username) == null;
            }
            sender().tell(new UserEligibleResult(res), self());
            getContext().stop(self());
        }).matchAny(this::unhandled).build());

    }

    public static class UserEligibleResult {

        public final boolean isValid;

        public UserEligibleResult(boolean isValid) {
            this.isValid = isValid;
        }
    }

}
