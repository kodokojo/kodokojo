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
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectBuilder;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.right.RightEndpointActor;
import io.kodokojo.service.repository.ProjectRepository;

import static akka.event.Logging.getLogger;
import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectCreatorActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(ProjectCreatorActor.class, projectRepository);
    }

    private ProjectCreateMsg originalMsg;

    private ActorRef originalSender;

    private ProjectConfiguration projectConfiguration;

    public ProjectCreatorActor(ProjectRepository projectRepository) {
        receive(ReceiveBuilder.match(ProjectCreateMsg.class, msg -> {
            originalMsg = msg;
            originalSender = sender();
            if (msg.getRequester() != null) {
                projectConfiguration = projectRepository.getProjectConfigurationById(msg.project.getProjectConfigurationIdentifier());
                getContext().actorOf(RightEndpointActor.PROPS()).tell(new RightEndpointActor.UserAdminRightRequestMsg(msg.getRequester(), projectConfiguration), self());
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Add project '{}' from unknown requester.", msg.project.getName());
                    LOGGER.debug("Add project {}", msg.project);
                }
                projectRepository.addProject(originalMsg.project, originalMsg.projectConfigurationIdentifier);
                getContext().stop(self());
            }
        })
                .match(RightEndpointActor.RightRequestResultMsg.class, msg -> {
                    UserRequestMessage result = null;
                    if (msg.isValid()) {
                        String projectId = projectRepository.addProject(originalMsg.project, originalMsg.projectConfigurationIdentifier);
                        ProjectBuilder builder = new ProjectBuilder(originalMsg.project);
                        builder.setIdentifier(projectId);
                        result = new ProjectCreateResultMsg(originalMsg.getRequester(), builder.build());
                    } else {
                        result = new ProjectCreateNotAuthoriseMsg(originalMsg.getRequester(), originalMsg.project);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("User {} isn't authorised to update project {}.", originalMsg.getRequester().getUsername(), originalMsg.project.getName());
                        }
                    }
                    originalSender.tell(result, self());
                    getContext().stop(self());
                })
                .matchAny(this::unhandled).build());
    }

    public static class ProjectCreateMsg extends UserRequestMessage {

        private final Project project;

        private final String projectConfigurationIdentifier;

        public ProjectCreateMsg(User requester, Project project, String projectConfigurationIdentifier) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            if (isBlank(projectConfigurationIdentifier)) {
                throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
            }
            this.project = project;
            this.projectConfigurationIdentifier = projectConfigurationIdentifier;
        }
    }

    public static class ProjectCreateResultMsg extends UserRequestMessage {

        private final Project project;

        public ProjectCreateResultMsg(User requester, Project project) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

    public static class ProjectCreateNotAuthoriseMsg extends UserRequestMessage {

        private final Project project;

        public ProjectCreateNotAuthoriseMsg(User requester, Project project) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

}
