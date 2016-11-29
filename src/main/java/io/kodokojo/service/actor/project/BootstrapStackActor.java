/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.BootstrapStackData;
import io.kodokojo.commons.model.StackType;
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

            int sshPortEntrypoint = 0;
            if (stackType == StackType.BUILD) {
                sshPortEntrypoint = bootstrapConfigurationProvider.provideTcpPortEntrypoint(projectName, stackName);
            }
            BootstrapStackData res = new BootstrapStackData(projectName, sshPortEntrypoint);
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
