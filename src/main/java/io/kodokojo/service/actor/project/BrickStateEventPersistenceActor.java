package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectBuilder;
import io.kodokojo.model.Stack;
import io.kodokojo.model.StackType;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.repository.ProjectRepository;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static akka.event.Logging.getLogger;

public class BrickStateEventPersistenceActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(BrickStateEventPersistenceActor.class, projectRepository);
    }

    private ActorRef originalSender;


    public BrickStateEventPersistenceActor(ProjectRepository projectRepository) {
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
            getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new ProjectUpdaterActor.ProjectUpdateMsg(null, builder.build()), self());

        }).match(ProjectUpdaterActor.ProjectUpdateResultMsg.class, msg -> {
            originalSender.tell(Futures.successful(Boolean.TRUE), self());
            getContext().stop(self());
        }).match(ProjectUpdaterActor.ProjectUpdateNotAuthoriseMsg.class, msg -> {
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
