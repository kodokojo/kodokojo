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
package io.kodokojo.service.actor.right;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.ProjectConfiguration;
import io.kodokojo.commons.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;
import org.apache.commons.collections4.IteratorUtils;

import java.util.List;

import static akka.event.Logging.getLogger;

public class RightEndpointActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final String NAME = "rightEndpointProps";

    public static Props PROPS() {
        return Props.create(RightEndpointActor.class);
    }

    public RightEndpointActor() {
        receive(ReceiveBuilder.match(UserAdminRightRequestMsg.class, msg -> {
            ProjectConfiguration projectConfiguration = msg.projectConfiguration;
            User user = msg.getRequester();
            List<User> users = IteratorUtils.toList(projectConfiguration.getAdmins());
            boolean valid = users.stream().filter(u -> u.getIdentifier().equals(user.getIdentifier())).findFirst().isPresent();
            sender().tell(new RightRequestResultMsg(user, msg, valid), self());
            getContext().stop(self());
        })
        .matchAny(this::unhandled).build());
    }

    //  Taging class.
    public static class RightRequestMsg extends  UserRequestMessage {
        public RightRequestMsg(User requester) {
            super(requester);
        }
    }

    public static class UserAdminRightRequestMsg extends RightRequestMsg {

        private final ProjectConfiguration projectConfiguration;

        public UserAdminRightRequestMsg(User requester, ProjectConfiguration projectConfiguration) {
            super(requester);
            if (projectConfiguration == null) {
                throw new IllegalArgumentException("projectConfiguration must be defined.");
            }
            this.projectConfiguration = projectConfiguration;
        }

        public ProjectConfiguration getProjectConfiguration() {
            return projectConfiguration;
        }
    }

    public static class RightRequestResultMsg extends UserRequestMessage {

        private final UserRequestMessage request;

        private final boolean valid;

        public RightRequestResultMsg(User requester, UserRequestMessage request, boolean valid) {
            super(requester);
            if (request == null) {
                throw new IllegalArgumentException("request must be defined.");
            }
            this.request = request;
            this.valid = valid;
        }

        public UserRequestMessage getRequest() {
            return request;
        }

        public boolean isValid() {
            return valid;
        }
    }


}
