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
package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.Entity;
import io.kodokojo.model.User;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.service.actor.EmailSenderActor;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.entity.AddUserToEntityActor;
import io.kodokojo.service.actor.entity.EntityCreatorActor;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.repository.UserRepository;
import org.apache.commons.lang.StringUtils;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import static akka.event.Logging.getLogger;
import static org.apache.commons.lang.StringUtils.isBlank;

public class UserCreatorActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(UserRepository userRepository, EmailSender emailSender) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        return Props.create(UserCreatorActor.class, userRepository, emailSender);
    }

    private final UserRepository userRepository;

    private final EmailSender emailSender;

    private boolean isValid = false;

    private KeyPair keyPair;

    private String password = "";

    private String entityId;

    private UserCreateMsg message;

    private ActorRef originalActor;

    public UserCreatorActor(UserRepository userRepository, EmailSender emailSender) {
        if (userRepository == null) {
            throw new IllegalArgumentException(" must be defined.");
        }
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        receive(ReceiveBuilder.match(UserCreateMsg.class, u -> {
            originalActor = sender();
            message = u;
            getContext().actorOf(UserGenerateSecurityData.PROPS()).tell(new UserGenerateSecurityData.GenerateSecurityMsg(), self());
            getContext().actorOf(UserEligibleActor.PROPS(userRepository)).tell(u, self());
            if (StringUtils.isBlank(u.entityId)) {
                Entity entity = new Entity(u.email);
                getContext().actorSelection(EndpointActor.ACTOR_PATH).tell(new EntityCreatorActor.EntityCreateMsg(entity), self());

            } else {
                entityId = u.entityId;
            }
        }).match(EntityCreatorActor.EntityCreatedResultMsg.class, msg -> {
            entityId = msg.getEntityId();
            getContext().actorSelection(EndpointActor.ACTOR_PATH).tell(new AddUserToEntityActor.AddUserToEntityMsg(null, message.id, entityId), self());
            isReadyToStore();
        })
                .match(UserEligibleActor.UserEligibleResultMsg.class, r -> {
                    isValid = r.isValid;
                    if (isValid) {
                        isReadyToStore();
                    } else {
                        originalActor.forward(r, getContext());
                        getContext().stop(self());
                    }
                })
                .match(UserGenerateSecurityData.UserSecurityDataMsg.class, msg -> {
                    password = msg.getPassword();
                    keyPair = msg.getKeyPair();
                    isReadyToStore();
                })
                .build());
    }

    private void isReadyToStore() {
        if (isValid && keyPair != null && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(entityId)) {
            User user = new User(message.id, entityId, message.username, message.username, message.email, password, RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), message.email));
            boolean added = userRepository.addUser(user);
            if (added) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("User {} successfully created.", message.getUsername());
                }
                originalActor.tell(new UserCreateResultMsg(message.getRequester(), user, keyPair), self());
                List<String> to = new ArrayList<>();
                to.add(message.getEmail());
                if (message.getRequester() != null) {
                    to.add(message.getRequester().getEmail());
                }
                // TODO: use velocity to use html template to create the content of Email.
                String content = "<h1>Welcome on Kodo Kojo</h1>\n" +
                        "<p>You will find all information which is bind to your account '" + message.getUsername() + "'.</p>\n" +
                        "\n" +
                        "<p>Password : <b>" + password + "</b></p>\n" +
                        "<p>Your SSH private key generated:\n" +
                        "<br />\n" +
                        keyPair.getPrivate().toString() + "\n" +
                        "</p>\n" +
                        "<p>Your SSH public key generated:\n" +
                        "<br />\n" +
                        user.getSshPublicKey() + "\n" +
                        "</p>";
                EmailSenderActor.EmailSenderMsg emailSenderMsg = new EmailSenderActor.EmailSenderMsg(to, String.format("Kodo Kojo user %s created", user.getUsername()), content);
                getContext().actorOf(EmailSenderActor.PROPS(emailSender)).tell(emailSenderMsg, self());
                getContext().stop(self());
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to store user {}", user);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Not yet ready to store the user.");
        }
    }

    public static class UserCreateMsg extends UserRequestMessage {

        protected final String id;

        protected final String email;

        protected final String username;

        protected final String entityId;

        public UserCreateMsg(User requester, String id, String email, String username, String entityId) {
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
            this.entityId = entityId;
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getUsername() {
            return username;
        }

        public String getEntityId() {
            return entityId;
        }
    }

    public static class UserCreateResultMsg extends UserRequestMessage {

        private final User user;

        private final KeyPair keyPair;

        public UserCreateResultMsg(User requester, User user, KeyPair keyPair) {
            super(requester);
            if (user == null) {
                throw new IllegalArgumentException("user must be defined.");
            }
            if (keyPair == null) {
                throw new IllegalArgumentException("keyPair must be defined.");
            }
            this.user = user;
            this.keyPair = keyPair;
        }

        public User getUser() {
            return user;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }
    }

}
