package io.kodokojo.brick;

import io.kodokojo.model.BrickState;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectBuilder;
import io.kodokojo.model.Stack;
import io.kodokojo.service.store.ProjectStore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StoreBrickStateListener implements BrickStateMsgListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreBrickStateListener.class);

    private final ProjectStore projectStore;

    @Inject
    public StoreBrickStateListener(ProjectStore projectStore) {
        if (projectStore == null) {
            throw new IllegalArgumentException("projectStore must be defined.");
        }
        this.projectStore = projectStore;
    }

    @Override
    public void receive(BrickState brickState) {
        if (brickState == null) {
            throw new IllegalArgumentException("brickState must be defined.");
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Receive following message : {}", brickState);
        }

        Project project = projectStore.getProjectByProjectConfigurationId(brickState.getProjectConfigurationIdentifier());
        if (project != null) {
            ProjectBuilder builder = new ProjectBuilder(project);
            Set<Stack> stacks = new HashSet<>();
            Set<Stack> projectStacks = project.getStacks();
            Iterator<Stack> stackIterator = projectStacks.iterator();
            boolean foundStack = false;
            boolean foundBrick = false;
            Set<BrickState> states = new HashSet<>();
            while (!foundStack && stackIterator.hasNext()) {
                Stack stack = stackIterator.next();
                if (stack.getName().equals(brickState.getStackName())) {
                    Iterator<BrickState> brickStateIterator = stack.getBrickStates().iterator();
                    foundStack = true;
                    while (!foundBrick && brickStateIterator.hasNext()) {
                        BrickState state = brickStateIterator.next();
                        if (state.getBrickName().equals(brickState.getBrickName())) {
                            states.add(brickState);
                            builder.setSnapshotDate(new Date());
                            foundBrick = true;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("State may change for project {}, override following state {}", project.getName(), brickState);
                            }
                        } else {
                            states.add(state);
                        }
                    }

                    if (!foundBrick) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Unable to found brick for this event, adding a new State.");
                        }
                        states.add(brickState);
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
            projectStore.updateProject(project);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unable to find project with project configuration id '{}'.", brickState.getProjectConfigurationIdentifier());
        }

    }
}
