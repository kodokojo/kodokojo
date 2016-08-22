package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.UserRepository;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserGenerateIdentifierActor extends AbstractActor {

    public static Props PROPS(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        return Props.create(UserGenerateIdentifierActor.class, userRepository);
    }

    public UserGenerateIdentifierActor(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }

        receive(ReceiveBuilder.match(UserGenerateIdentifierMsg.class, msg -> {

            String generateId = userRepository.generateId();
            sender().tell(new UserGenerateIdentifierResultMsg(generateId), self());
            getContext().stop(self());

        }).matchAny(this::unhandled).build());
    }

    public static class UserGenerateIdentifierMsg {

    }

    public static class UserGenerateIdentifierResultMsg {

        private final String generateId;

        public UserGenerateIdentifierResultMsg(String generateId) {
            if (isBlank(generateId)) {
                throw new IllegalArgumentException("generateId must be defined.");
            }
            this.generateId = generateId;
        }

        public String getGenerateId() {
            return generateId;
        }
    }

}
