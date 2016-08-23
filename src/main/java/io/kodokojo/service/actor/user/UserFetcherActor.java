package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.repository.UserFetcher;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;

public class UserFetcherActor extends AbstractActor {

    public static Props PROPS(UserFetcher userFetcher) {
        if (userFetcher == null) {
            throw new IllegalArgumentException("userFetcher must be defined.");
        }
        return Props.create(UserFetcherActor.class, userFetcher);
    }

    public UserFetcherActor(UserFetcher userFetcher) {
        receive(ReceiveBuilder
                .match(UserFetchMsg.class, msg -> {
                    Set<User> users = msg.userIds.stream()
                            .map(userFetcher::getUserByIdentifier)
                            .map(u -> new User(u.getIdentifier(), u.getEntityIdentifier(), u.getFirstName(), u.getLastName(), u.getUsername(), u.getEmail(), "", u.getSshPublicKey()))
                            .collect(Collectors.toSet());
                    sender().tell(new UserFetchResultMsg(msg.getRequester(), users), self());
                    getContext().stop(self());
                })
                .matchAny(this::unhandled).build());
    }

    public static class UserFetchMsg extends UserRequestMessage {

        private final Set<String> userIds;

        public UserFetchMsg(User requester, Set<String> userIds) {
            super(requester);
            this.userIds = userIds;
        }

        public UserFetchMsg(User requester, String userId) {
            this(requester, Collections.singleton(userId));
        }
    }

    public static class UserFetchResultMsg extends UserRequestMessage {

        private final Set<User> users;

        public UserFetchResultMsg(User requester, Set<User> users) {
            super(requester);
            this.users = users;
        }

        public Set<User> getUsers() {
            return users;
        }
    }

}
