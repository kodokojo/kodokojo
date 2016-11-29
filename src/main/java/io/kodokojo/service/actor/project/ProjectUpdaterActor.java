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
package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.ProjectConfiguration;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.right.RightEndpointActor;
import io.kodokojo.service.repository.ProjectRepository;

import java.util.Objects;
import java.util.Optional;

import static akka.event.Logging.getLogger;

public class ProjectUpdaterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    private ProjectUpdaterMessages.ProjectUpdateMsg originalMsg;

    private ActorRef originalSender;

    private ProjectRepository projectRepository;

    public ProjectUpdaterActor(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        receive(ReceiveBuilder
                .match(ProjectUpdaterMessages.ProjectUpdateMsg.class, this::onProjectUpdate)
                .match(RightEndpointActor.RightRequestResultMsg.class, this::onRightRequestResult)
                .matchAny(this::unhandled)
                .build());
    }

    public static final Props props(ProjectRepository projectRepository) {
        Objects.requireNonNull(projectRepository, "projectRepository must be defined.");
        return Props.create(ProjectUpdaterActor.class, projectRepository);
    }

    private void onRightRequestResult(RightEndpointActor.RightRequestResultMsg msg) {
        UserRequestMessage result;
        if (msg.isValid()) {
            projectRepository.updateProject(originalMsg.project);
            result = new ProjectUpdaterMessages.ProjectUpdateResultMsg(originalMsg.getRequester(), originalMsg.project);
        } else {
            result = new ProjectUpdaterMessages.ProjectUpdateNotAuthoriseMsg(originalMsg.getRequester(), originalMsg.project);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("User {} isn't authorised to update project {}.", originalMsg.getRequester().getUsername(), originalMsg.project.getName());
            }
        }
        Optional.ofNullable(originalSender).ifPresent((sender) -> originalSender.tell(result, self()));
        getContext().stop(self());
    }

    private void onProjectUpdate(ProjectUpdaterMessages.ProjectUpdateMsg msg) {
        originalMsg = msg;
        originalSender = sender();
        if (msg.getRequester() != null) {
            ProjectConfiguration projectConfiguration = projectRepository.getProjectConfigurationById(msg.project.getProjectConfigurationIdentifier());
            getContext().actorOf(RightEndpointActor.PROPS()).tell(new RightEndpointActor.UserAdminRightRequestMsg(msg.getRequester(), projectConfiguration), self());
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Update project '{}' from unknown requester.", msg.project.getName());
                LOGGER.debug("Update project {}", msg.project);
            }
            projectRepository.updateProject(originalMsg.project);
            getContext().stop(self());
        }
    }

}
