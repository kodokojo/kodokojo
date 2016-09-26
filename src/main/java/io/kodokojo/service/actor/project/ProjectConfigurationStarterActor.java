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
import akka.dispatch.Futures;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.*;
import io.kodokojo.service.ProjectAlreadyExistException;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.right.RightEndpointActor;
import io.kodokojo.service.repository.ProjectRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;

public class ProjectConfigurationStarterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(ProjectRepository projectRepository) {
        return Props.create(ProjectConfigurationStarterActor.class, projectRepository);
    }

    private ActorRef originalSender;

    private ProjectConfigurationStartMsg initialMsg;

    public ProjectConfigurationStarterActor(ProjectRepository projectRepository) {
        receive(ReceiveBuilder
                .match(ProjectConfigurationStartMsg.class, msg -> {
                    originalSender = sender();
                    initialMsg = msg;
                    LOGGER.debug("Receive project configuration start request, check project right.");
                    getContext().actorOf(RightEndpointActor.PROPS()).tell(new RightEndpointActor.UserAdminRightRequestMsg(msg.getRequester(), msg.projectConfiguration), self());

                }).match(RightEndpointActor.RightRequestResultMsg.class, msg -> {
                            LOGGER.debug("Receive right response : {}.", msg.isValid());
                            if (msg.isValid()) {
                                ProjectConfiguration projectConfiguration = initialMsg.projectConfiguration;
                                Project project = projectRepository.getProjectByProjectConfigurationId(projectConfiguration.getIdentifier());
                                if (project == null) {
                                    Set<Stack> stacks = new HashSet<>();

                                    StackConfiguration defaultStackConfiguration = projectConfiguration.getDefaultStackConfiguration();
                                    Stack stack = new Stack(defaultStackConfiguration.getName(), defaultStackConfiguration.getType(), new HashSet<>());
                                    Set<BrickStateEvent> brickStateEvents = defaultStackConfiguration.getBrickConfigurations().stream()
                                            .map(b -> new BrickStateEvent(projectConfiguration.getIdentifier(),
                                                    stack.getName(),
                                                    b.getType().toString(),
                                                    b.getName(),
                                                    BrickStateEvent.State.UNKNOWN,
                                                    b.getVersion())
                                            ).collect(Collectors.toSet());
                                    stack.getBrickStateEvents().addAll(brickStateEvents);
                                    stacks.add(stack);
                                    Project res = new Project(projectConfiguration.getIdentifier(), projectConfiguration.getName(), new Date(), stacks);
                                    getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new ProjectCreatorActor.ProjectCreateMsg(initialMsg.getRequester(), res, projectConfiguration.getIdentifier()), self());


                                } else {
                                    sender().tell(Futures.failed(new ProjectAlreadyExistException(projectConfiguration.getName())), self());
                                    getContext().stop(self());
                                }
                            }
                        }

                )
                .match(ProjectCreatorActor.ProjectCreateResultMsg.class, msg -> {
                    originalSender.forward(msg, getContext());
                    ProjectConfiguration projectConfiguration = initialMsg.projectConfiguration;
                    StackConfiguration defaultStackConfiguration = projectConfiguration.getDefaultStackConfiguration();
                    getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new StackConfigurationStarterActor.StackConfigurationStartMsg(projectConfiguration, defaultStackConfiguration), ActorRef.noSender());
                })
                .match(StackConfigurationStarterActor.StackConfigurationStartResultMsg.class, msg -> {
                    if (msg.isSuccess()) {
                        LOGGER.info("Project {} successfully started.", msg.getProjectConfiguration().getName());
                    } else {
                        LOGGER.error("Project {} failed to start.", msg.getProjectConfiguration().getName());
                    }
                    getContext().stop(self());
                })
                .matchAny(this::unhandled).build());
    }

    public static class ProjectConfigurationStartMsg extends UserRequestMessage {

        private final ProjectConfiguration projectConfiguration;

        public ProjectConfigurationStartMsg(User requester, ProjectConfiguration projectConfiguration) {
            super(requester);
            if (projectConfiguration == null) {
                throw new IllegalArgumentException("projectConfiguration must be defined.");
            }
            this.projectConfiguration = projectConfiguration;
        }
    }

    public static class ProjectConfigurationStartResultMsg {

        private final Project project;

        public ProjectConfigurationStartResultMsg(Project project) {
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

}
