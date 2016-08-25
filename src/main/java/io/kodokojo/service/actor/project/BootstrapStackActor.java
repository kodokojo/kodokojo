package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.BootstrapStackData;
import io.kodokojo.model.StackType;
import io.kodokojo.service.ProjectManager;

import static akka.event.Logging.getLogger;
import static org.apache.commons.lang.StringUtils.isBlank;

public class BootstrapStackActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(ProjectManager projectManager) {
        if (projectManager == null) {
            throw new IllegalArgumentException("projectManager must be defined.");
        }
        return Props.create(BootstrapStackActor.class, projectManager);
    }

    public BootstrapStackActor(ProjectManager projectManager) {
        receive(ReceiveBuilder.match(BootstrapStackMsg.class,  msg -> {
            LOGGER.debug("Boostraping project '{}'", msg.projectName);
            BootstrapStackData bootstrapStackData = projectManager.bootstrapStack(msg.projectName, msg.stackName, msg.stackType);
            sender().tell(new BootstrapStackResultMsg(bootstrapStackData), self());
            getContext().stop(self());
        }).matchAny(this::unhandled).build());

    }

    public static class BootstrapStackMsg {

        private final String projectName;

        private final String stackName;

        private final StackType stackType;

        public BootstrapStackMsg(String projectName, String stackName, StackType stackType) {
            if (isBlank(projectName)) {
                throw new IllegalArgumentException("projectName must be defined.");
            }
            if (isBlank(stackName)) {
                throw new IllegalArgumentException("stackName must be defined.");
            }
            if (stackType == null) {
                throw new IllegalArgumentException("stackType must be defined.");
            }
            this.projectName = projectName;
            this.stackName = stackName;
            this.stackType = stackType;
        }
    }


    public static class BootstrapStackResultMsg {

        private final BootstrapStackData bootstrapStackData;

        public BootstrapStackResultMsg(BootstrapStackData bootstrapStackData) {
            this.bootstrapStackData = bootstrapStackData;
        }

        public BootstrapStackData getBootstrapStackData() {
            return bootstrapStackData;
        }
    }

}
