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
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.right.RightEndpointActor;
import io.kodokojo.service.repository.ProjectRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
