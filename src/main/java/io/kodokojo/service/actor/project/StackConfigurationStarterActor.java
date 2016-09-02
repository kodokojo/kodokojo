package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.BrickStateEvent;

import static akka.event.Logging.getLogger;

public class StackConfigurationStarterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS() {
        return Props.create(StackConfigurationStarterActor.class);
    }


    private StackConfigurationStartMsg intialMsg;

    private ActorRef originalSender;

    private int nbResponse = 0;

    public StackConfigurationStarterActor() {
        receive(ReceiveBuilder.match(StackConfigurationStartMsg.class, msg -> {
            intialMsg = msg;
            originalSender = sender();
            ActorRef endpointActor = getContext().actorFor(EndpointActor.ACTOR_PATH);
            msg.stackConfiguration.getBrickConfigurations().forEach(b -> {
                endpointActor.tell(new BrickStartContext(msg.projectConfiguration, msg.stackConfiguration, b),  self());
            });

        })
        .match(BrickStateEvent.class, msg -> {
            if(msg.getState() == BrickStateEvent.State.RUNNING ||
                    msg.getState() == BrickStateEvent.State.ONFAILURE ||
                    msg.getState() == BrickStateEvent.State.ALREADYEXIST
                    ) {
            nbResponse++;
            }
            if (nbResponse == intialMsg.stackConfiguration.getBrickConfigurations().size()) {
                originalSender.tell(new StackConfigurationStartResultMsg(intialMsg.projectConfiguration, intialMsg.stackConfiguration, true), self());
                getContext().stop(self());
            }
        })
                .matchAny(this::unhandled).build());
    }

    public static class StackConfigurationStartMsg {

        private final ProjectConfiguration projectConfiguration;

        private final StackConfiguration stackConfiguration;

        public StackConfigurationStartMsg(ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration) {

            if (projectConfiguration == null) {
                throw new IllegalArgumentException("projectConfiguration must be defined.");
            }
            if (stackConfiguration == null) {
                throw new IllegalArgumentException("stackConfiguration must be defined.");
            }
            this.projectConfiguration = projectConfiguration;
            this.stackConfiguration = stackConfiguration;
        }
    }
    public static class StackConfigurationStartResultMsg {
        private final ProjectConfiguration projectConfiguration;

        private final StackConfiguration stackConfiguration;

        private boolean success;

        public StackConfigurationStartResultMsg(ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, boolean success) {

            if (projectConfiguration == null) {
                throw new IllegalArgumentException("projectConfiguration must be defined.");
            }
            if (stackConfiguration == null) {
                throw new IllegalArgumentException("stackConfiguration must be defined.");
            }
            this.projectConfiguration = projectConfiguration;
            this.stackConfiguration = stackConfiguration;
            this.success = success;
        }

        public ProjectConfiguration getProjectConfiguration() {
            return projectConfiguration;
        }

        public StackConfiguration getStackConfiguration() {
            return stackConfiguration;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}
