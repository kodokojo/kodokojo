package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.User;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.service.actor.UserRequestMessage;
import io.kodokojo.service.repository.UserRepository;
import org.apache.commons.lang.StringUtils;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserCreatorActor extends AbstractActor {

    public static Props PROPS(UserRepository userRepository) {
        return Props.create(UserCreatorActor.class, userRepository);
    }

    private final UserRepository userRepository;

    private boolean isValid = false;

    private KeyPair keyPair;

    private String password = "";

    private UserCreateMsg message;

    public UserCreatorActor(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException(" must be defined.");
        }
        this.userRepository = userRepository;
        receive(ReceiveBuilder.match(UserCreateMsg.class, u -> {
            message = u;
            getContext().actorOf(UserEligibleActor.PROPS(userRepository)).tell(u, self());
            getContext().actorOf(UserGenerateSecurityData.PROPS()).tell(u, self());
        })
                .match(UserEligibleActor.UserEligibleResult.class, r -> {
                    isValid = r.isValid;
                    isReadyToStore();
                })
                .match(UserGenerateSecurityData.UserSecurityDataMessage.class, msg -> {
                    password = msg.getPassword();
                    keyPair = msg.getKeyPair();
                    isReadyToStore();
                })
                .build());
    }

    private void isReadyToStore() {
        if (isValid && keyPair != null && StringUtils.isNotBlank(password)) {
            User user = new User(message.id, message.username, message.username, message.email, password, RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), message.email));
            boolean added = userRepository.addUser(user);
            if (added) {
                sender().tell(new UserCreatedMessage(message.getRequester(), user, keyPair, message.entityName), self());
                getContext().stop(self());
            }
        }
    }

    public static class UserCreateMsg extends UserRequestMessage {

        protected final String id;

        protected final String email;

        protected final String username;

        protected final String entityName;

        public UserCreateMsg(User requester, String id, String email, String username, String entityName) {
            super(requester);
            if (isBlank(id)) {
                throw new IllegalArgumentException("id must be defined.");
            }
            if (isBlank(email)) {
                throw new IllegalArgumentException("email must be defined.");
            }

            if (isBlank(username)) {
                throw new IllegalArgumentException("username must be defined.");
            }
            this.id = id;
            this.email = email;
            this.username = username;
            this.entityName = entityName;
        }

    }

    public static class UserCreatedMessage extends UserRequestMessage {

        private final User user;

        private final KeyPair keyPair;

        private final String entityNameRequested;

        public UserCreatedMessage(User requester, User user, KeyPair keyPair, String entityNameRequested) {
            super(requester);
            if (user == null) {
                throw new IllegalArgumentException("user must be defined.");
            }
            if (keyPair == null) {
                throw new IllegalArgumentException("keyPair must be defined.");
            }
            if (isBlank(entityNameRequested)) {
                throw new IllegalArgumentException("entityNameRequested must be defined.");
            }
            this.user = user;
            this.keyPair = keyPair;
            this.entityNameRequested = entityNameRequested;
        }

        public User getUser() {
            return user;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        public String getEntityNameRequested() {
            return entityNameRequested;
        }
    }

}
