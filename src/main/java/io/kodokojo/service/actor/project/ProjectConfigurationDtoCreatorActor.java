package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.endpoint.dto.ProjectCreationDto;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.repository.ProjectRepository;

import static akka.event.Logging.getLogger;

public class ProjectConfigurationDtoCreatorActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(ProjectConfigurationDtoCreatorActor.class, projectRepository);
    }

    private ActorRef originalSender;

    private ProjectConfigurationDtoCreateMsg initialMsg;

    public ProjectConfigurationDtoCreatorActor(ProjectRepository projectRepository) {
        receive(ReceiveBuilder.match(ProjectConfigurationDtoCreateMsg.class, msg -> {
            originalSender = sender();
            initialMsg = msg;
            LOGGER.debug("Receive a projectConfigDto to create with project name '{}'.", msg.projectConfigDto.getName());
            ProjectConfigurationBuilderActor.ProjectConfigurationBuildMsg projectConfigurationBuildMsg = new ProjectConfigurationBuilderActor.ProjectConfigurationBuildMsg(msg.getRequester(), msg.projectConfigDto);
            getContext().actorSelection(EndpointActor.ACTOR_PATH).tell(projectConfigurationBuildMsg, self());
        }).match(ProjectConfigurationBuilderActor.ProjectConfigurationBuildResultMsg.class, msg -> {
            LOGGER.debug("Receive a projectConfiguration to add to store.");
            ProjectConfiguration projectConfiguration = msg.getProjectConfiguration();
            String projectConfigurationId = projectRepository.addProjectConfiguration(projectConfiguration);
            originalSender.tell(new ProjectConfigurationDtoCreateResultMsg(initialMsg.getRequester(), projectConfigurationId), self());
        }).matchAny(this::unhandled).build());
    }

    public static class ProjectConfigurationDtoCreateMsg extends UserRequestMessage {

        private final ProjectCreationDto projectConfigDto;

        public ProjectConfigurationDtoCreateMsg(User requester, ProjectCreationDto projectCreationDto) {
            super(requester);
            if (projectCreationDto == null) {
                throw new IllegalArgumentException("projectCreationDto must be defined.");
            }
            this.projectConfigDto = projectCreationDto;
        }
    }

    public static class ProjectConfigurationDtoCreateResultMsg extends UserRequestMessage {

        private final String projectConfigurationId;

        public ProjectConfigurationDtoCreateResultMsg(User requester, String projectConfigurationId) {
            super(requester);
            this.projectConfigurationId = projectConfigurationId;
        }

        public String getProjectConfigurationId() {
            return projectConfigurationId;
        }
    }
}
