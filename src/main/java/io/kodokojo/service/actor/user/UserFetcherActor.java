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
                    sender().tell(new UserFetchResultMsg(msg.getRequester(),msg.userIds,  users), self());
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

        private final Set<String> userIdRequeted;

        private final Set<User> users;

        public UserFetchResultMsg(User requester, Set<String> userIdRequeted, Set<User> users) {
            super(requester);
            this.userIdRequeted = userIdRequeted;
            this.users = users;
        }

        public Set<String> getUserIdRequeted() {
            return userIdRequeted;
        }

        public Set<User> getUsers() {
            return users;
        }
    }

}
