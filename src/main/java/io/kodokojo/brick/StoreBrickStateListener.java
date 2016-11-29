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
package io.kodokojo.brick;

import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.commons.model.Project;
import io.kodokojo.commons.model.ProjectBuilder;
import io.kodokojo.commons.model.Stack;
import io.kodokojo.service.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StoreBrickStateListener implements BrickStateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreBrickStateListener.class);

    private final ProjectRepository projectRepository;

    @Inject
    public StoreBrickStateListener(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        this.projectRepository = projectRepository;
    }

    @Override
    public void receive(BrickStateEvent brickStateEvent) {
        if (brickStateEvent == null) {
            throw new IllegalArgumentException("brickStateEvent must be defined.");
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Receive following message : {}", brickStateEvent);
        }

        Project project = projectRepository.getProjectByProjectConfigurationId(brickStateEvent.getProjectConfigurationIdentifier());
        if (project != null) {
            ProjectBuilder builder = new ProjectBuilder(project);
            Set<Stack> stacks = new HashSet<>();
            Set<Stack> projectStacks = project.getStacks();
            Iterator<Stack> stackIterator = projectStacks.iterator();
            boolean foundStack = false;
            boolean foundBrick = false;
            Set<BrickStateEvent> states = new HashSet<>();
            while (!foundStack && stackIterator.hasNext()) {
                Stack stack = stackIterator.next();
                if (stack.getName().equals(brickStateEvent.getStackName())) {
                    Iterator<BrickStateEvent> brickStateIterator = stack.getBrickStateEvents().iterator();
                    foundStack = true;
                    while (!foundBrick && brickStateIterator.hasNext()) {
                        BrickStateEvent state = brickStateIterator.next();
                        if (state.getBrickName().equals(brickStateEvent.getBrickName())) {
                            states.add(brickStateEvent);
                            builder.setSnapshotDate(new Date());
                            foundBrick = true;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("State may change for project {}, override following state {}", project.getName(), brickStateEvent);
                            }
                        } else {
                            states.add(state);
                        }
                    }

                    if (!foundBrick) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Unable to found brick for this event, adding a new State.");
                        }
                        states.add(brickStateEvent);
                    }
                } else {
                    stacks.add(stack);
                }
                stacks.add(new Stack(stack.getName(), stack.getStackType(), states));
            }
            if (!foundStack) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to found stack for this event.");
                }
            }
            builder.setStacks(stacks);

            project = builder.build();
            projectRepository.updateProject(project);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unable to find project with project configuration id '{}'.", brickStateEvent.getProjectConfigurationIdentifier());
        }

    }
}
