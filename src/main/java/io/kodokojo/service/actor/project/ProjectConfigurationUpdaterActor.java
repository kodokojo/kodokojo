package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.repository.ProjectRepository;

import static akka.event.Logging.getLogger;

public class ProjectConfigurationUpdaterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(ProjectConfigurationUpdaterActor.class, projectRepository);
    }

    public ProjectConfigurationUpdaterActor(ProjectRepository projectRepository) {
        receive(ReceiveBuilder
                .match(ProjectConfigurationUpdaterMsg.class, msg -> {
                    // Ask for right if UserRequester is defined.
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Update ProjectConfiguration {}", msg.projectConfiguration);
                    }
                    projectRepository.updateProjectConfiguration(msg.projectConfiguration);
                    sender().tell(new ProjectConfigurationUpdaterResultMsg(msg.getRequester(), msg.projectConfiguration), self());
                    getContext().stop(self());
                })
                .matchAny(this::unhandled).build());
    }

    public static class ProjectConfigurationUpdaterMsg extends UserRequestMessage {

        private final ProjectConfiguration projectConfiguration;

        public ProjectConfigurationUpdaterMsg(User requester, ProjectConfiguration projectConfiguration) {
            super(requester);
            if (projectConfiguration == null) {
                throw new IllegalArgumentException("projectConfiguration must be defined.");
            }
            this.projectConfiguration = projectConfiguration;
        }
    }
    public static class ProjectConfigurationUpdaterResultMsg extends UserRequestMessage {

        private final ProjectConfiguration projectConfiguration;

        public ProjectConfigurationUpdaterResultMsg(User requester, ProjectConfiguration projectConfiguration) {
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
}
