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
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.Project;
import io.kodokojo.commons.model.ProjectBuilder;
import io.kodokojo.commons.model.Stack;
import io.kodokojo.commons.model.StackType;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.repository.ProjectRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static akka.event.Logging.getLogger;

public class BrickStateEventPersistenceActor extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickStateEventPersistenceActor.class);

    public static Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(BrickStateEventPersistenceActor.class, projectRepository);
    }

    private ActorRef originalSender;


    public BrickStateEventPersistenceActor(ProjectRepository projectRepository) {
        LOGGER.debug("Create a new BrickStateEventPersistenceActor.");
        receive(ReceiveBuilder.match(BrickStateEvent.class, msg -> {
            LOGGER.debug("Receive BrickStateEvent for project configuration identifier {}.", msg.getProjectConfigurationIdentifier());
            originalSender = sender();
            Project project = projectRepository.getProjectByProjectConfigurationId(msg.getProjectConfigurationIdentifier());
            if (project == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to find project configuration id '{}'.", msg.getProjectConfigurationIdentifier());
                }
                getContext().stop(self());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Following project may be updated: {}", project);
            }

            ProjectBuilder builder = new ProjectBuilder(project).setSnapshotDate(new Date());
            Stack stack = findOrCreateStack(project, msg.getStackName());
            Set<Stack> stacks = new HashSet<>(project.getStacks());
            Set<BrickStateEvent> brickStateEvents = stack.getBrickStateEvents();
            Optional<BrickStateEvent> brickStateEvent = brickStateEvents.stream()
                    .filter(b -> b.getBrickName().equals(msg.getBrickName()) &&
                            b.getBrickType().equals(msg.getBrickType()))
                    .findFirst();
            String actionLog = "Adding";
            if (brickStateEvent.isPresent()) {
                actionLog = "Updating";
                brickStateEvents.remove(brickStateEvent.get());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} following state to project {} : {}", actionLog, project.getName(), msg);
            }
            brickStateEvents.add(msg);
            stacks.add(stack);

            builder.setStacks(stacks);
            getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new ProjectUpdaterMessages.ProjectUpdateMsg(null, builder.build()), self());

        }).match(ProjectUpdaterMessages.ProjectUpdateResultMsg.class, msg -> {
            originalSender.tell(Futures.successful(Boolean.TRUE), self());
            getContext().stop(self());
        }).match(ProjectUpdaterMessages.ProjectUpdateNotAuthoriseMsg.class, msg -> {
            LOGGER.error("Unexpected behavior happened when trying to update a project state from an brick state change notification.");
            getContext().stop(self());
        })
                .matchAny(this::unhandled).build());
    }

    protected static Stack findOrCreateStack(Project project, String stackName) {
        assert project != null : "project must be defined.";
        assert StringUtils.isNotBlank(stackName) : "stackName must be defined.";
        Optional<Stack> stack = project.getStacks().stream().filter(s -> s.getName().equals(stackName)).findFirst();
        if (stack.isPresent()) {
            return stack.get();
        }
        return new Stack(stackName, StackType.BUILD, new HashSet<>());
    }

}
