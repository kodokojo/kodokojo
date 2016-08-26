package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.right.RightEndpointActor;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserFetcher;

import static akka.event.Logging.getLogger;

public class ProjectUpdaterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final Props PROPS(ProjectRepository projectRepository, UserFetcher userFetcher) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (userFetcher == null) {
            throw new IllegalArgumentException("userFetcher must be defined.");
        }
        return Props.create(ProjectUpdaterActor.class, projectRepository, userFetcher);
    }

    private ProjectUpdateMsg originalMsg;

    private ActorRef originalSender;

    private ProjectConfiguration projectConfiguration;

    public ProjectUpdaterActor(ProjectRepository projectRepository, UserFetcher userFetcher) {
        receive(ReceiveBuilder.match(ProjectUpdateMsg.class, msg -> {
            originalMsg = msg;
            originalSender = sender();
            if (msg.getRequester() != null) {
                projectConfiguration = projectRepository.getProjectConfigurationById(msg.project.getProjectConfigurationIdentifier());
                getContext().actorOf(RightEndpointActor.PROPS(userFetcher)).tell(new RightEndpointActor.UserAdminRightRequestMsg(msg.getRequester(), projectConfiguration), self());
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Update project '{}' from unknown requester.", msg.project.getName());
                }
                projectRepository.updateProject(originalMsg.project);
                getContext().stop(self());
            }
        })
                .match(RightEndpointActor.RightRequestResultMsg.class, msg -> {
                    UserRequestMessage result = null;
                    if (msg.isValid()) {
                        projectRepository.updateProject(originalMsg.project);
                        result = new ProjectUpdateResultMsg(originalMsg.getRequester(), originalMsg.project);
                    } else {
                        result = new ProjectUpdateNotAuthoriseMsg(originalMsg.getRequester(), originalMsg.project);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("User {} isn't authorised to update project {}.", originalMsg.getRequester().getUsername(), originalMsg.project.getName());
                        }
                    }
                    originalSender.tell(result, self());
                    getContext().stop(self());
                })
                .matchAny(this::unhandled).build());
    }

    public static class ProjectUpdateMsg extends UserRequestMessage {

        private final Project project;

        public ProjectUpdateMsg(User requester, Project project) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            this.project = project;
        }
    }

    public static class ProjectUpdateResultMsg extends UserRequestMessage {

        private final Project project;

        public ProjectUpdateResultMsg(User requester, Project project) {
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

    public static class ProjectUpdateNotAuthoriseMsg extends UserRequestMessage {

        private final Project project;

        public ProjectUpdateNotAuthoriseMsg(User requester, Project project) {
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
