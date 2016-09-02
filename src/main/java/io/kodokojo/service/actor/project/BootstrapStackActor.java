package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.BootstrapStackData;
import io.kodokojo.model.StackType;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.ConfigurationStore;

import static akka.event.Logging.getLogger;
import static org.apache.commons.lang.StringUtils.isBlank;

public class BootstrapStackActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS( BootstrapConfigurationProvider bootstrapConfigurationProvider, ConfigurationStore configurationStore) {

        if (bootstrapConfigurationProvider == null) {
            throw new IllegalArgumentException("bootstrapConfigurationProvider must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        return Props.create(BootstrapStackActor.class, bootstrapConfigurationProvider, configurationStore);
    }

    public BootstrapStackActor(BootstrapConfigurationProvider bootstrapConfigurationProvider, ConfigurationStore configurationStore) {
        receive(ReceiveBuilder.match(BootstrapStackMsg.class,  msg -> {
            String projectName = msg.projectName;
            String stackName = msg.stackName;
            StackType stackType = msg.stackType;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Boostraping project '{}'", msg.projectName);
            }

            String loadBalancerHost = bootstrapConfigurationProvider.provideLoadBalancerHost(projectName, stackName);
            int sshPortEntrypoint = 0;
            if (stackType == StackType.BUILD) {
                sshPortEntrypoint = bootstrapConfigurationProvider.provideTcpPortEntrypoint(projectName, stackName);
            }
            BootstrapStackData res = new BootstrapStackData(projectName, stackName, loadBalancerHost, sshPortEntrypoint);
            configurationStore.storeBootstrapStackData(res);
            sender().tell(new BootstrapStackResultMsg(res), self());
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
