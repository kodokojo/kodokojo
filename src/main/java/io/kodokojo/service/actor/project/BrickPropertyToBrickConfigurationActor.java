package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.service.repository.ProjectRepository;

import java.io.Serializable;
import java.util.Map;

import static akka.event.Logging.getLogger;
import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickPropertyToBrickConfigurationActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(BrickPropertyToBrickConfigurationActor.class, projectRepository);
    }

    public BrickPropertyToBrickConfigurationActor(ProjectRepository projectRepository) {
        receive(ReceiveBuilder
                .match(BrickPropertyToBrickConfigurationMsg.class, msg -> {
                    ProjectConfiguration projectConfiguration = projectRepository.getProjectConfigurationById(msg.projectConfigurationIdentifier);
                    if (projectConfiguration != null) {
                        projectConfiguration.getStackConfigurations().stream().filter(s -> s.getName().equals(msg.stackName)).findFirst()
                                .ifPresent(s -> s.getBrickConfigurations().stream().filter(b -> b.getName().equals(msg.brickName)).findFirst()
                                        .ifPresent(b -> b.getProperties().putAll(msg.properties)));
                    }
                    projectRepository.updateProjectConfiguration(projectConfiguration);
                    sender().tell(new BrickPropertyToBrickConfigurationResultMsg(true), self());
                    getContext().stop(self());
                })
                .matchAny(this::unhandled).build());
    }

    public static class BrickPropertyToBrickConfigurationMsg {

        private final String projectConfigurationIdentifier;

        private final String stackName;

        private final String brickName;

        private final Map<String, Serializable> properties;

        public BrickPropertyToBrickConfigurationMsg(String projectConfigurationIdentifier, String stackName, String brickName, Map<String, Serializable> properties) {
            if (projectConfigurationIdentifier == null) {
                throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
            }
            if (properties == null) {
                throw new IllegalArgumentException("properties must be defined.");
            }
            if (isBlank(stackName)) {
                throw new IllegalArgumentException("stackName must be defined.");
            }
            if (isBlank(brickName)) {
                throw new IllegalArgumentException("brickName must be defined.");
            }
            this.stackName = stackName;
            this.brickName = brickName;
            this.projectConfigurationIdentifier = projectConfigurationIdentifier;
            this.properties = properties;
        }
    }


    public static class BrickPropertyToBrickConfigurationResultMsg {

        private final boolean success;

        public BrickPropertyToBrickConfigurationResultMsg(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}
