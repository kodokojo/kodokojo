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
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.UpdateData;
import io.kodokojo.commons.model.User;
import io.kodokojo.commons.model.UserBuilder;
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
        builder.setPassword(msg.getNewPassword())
                .setSshPublicKey(msg.getNewSSHPublicKey())
                .setFirstName(msg.getFirstName())
                .setLastName(msg.getLastName())
                .setEmail(msg.getEmail());
        User user = builder.build();
        userRepository.updateUser(user);
        sender().tell(new UserMessage.UserUpdateMessageResult(msg.getRequester(), true), self());
        getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg(msg.getRequester(), new UpdateData<>(oldUser, user)), self());
        getContext().stop(self());
    }

}
