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
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.model.PortDefinition;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public class StackConfigurationStarterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    //  Source Regexp http://sroze.io/2008/10/09/regex-ipv4-et-ipv6/
    private static final Pattern IP_PATTERN = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");


    public static Props PROPS(DnsManager dnsManager, BrickUrlFactory brickUrlFactory, ApplicationConfig applicationConfig) {
        requireNonNull(dnsManager, "dnsManager must be defined.");
        requireNonNull(brickUrlFactory, "brickUrlFactory must be defined.");
        requireNonNull(applicationConfig, "applicationConfig must be defined.");

        return Props.create(StackConfigurationStarterActor.class, dnsManager, brickUrlFactory, applicationConfig);
    }

    private StackConfigurationStartMsg intialMsg;

    private ActorRef originalSender;

    private int nbResponse = 0;

    public StackConfigurationStarterActor(DnsManager dnsManager, BrickUrlFactory brickUrlFactory, ApplicationConfig applicationConfig) {
        receive(ReceiveBuilder.match(StackConfigurationStartMsg.class, msg -> {
            intialMsg = msg;
            originalSender = sender();
            ActorRef endpointActor = getContext().actorFor(EndpointActor.ACTOR_PATH);

            Set<DnsEntry> dnsentries = new HashSet<>();
            msg.stackConfiguration.getBrickConfigurations().forEach(b -> {
                endpointActor.tell(new BrickStartContext(msg.projectConfiguration, msg.stackConfiguration, b), self());
                dnsentries.addAll(b.getPortDefinitions().stream()
                        .filter(p -> p.getType() == PortDefinition.Type.HTTP || p.getType() == PortDefinition.Type.HTTPS)
                        .map(p -> {
                            String entry = brickUrlFactory.forgeUrl(msg.projectConfiguration, msg.stackConfiguration.getName(), b);
                            DnsEntry dnsEntry = new DnsEntry(entry, getDnsType(applicationConfig.loadbalancerHost()), applicationConfig.loadbalancerHost());
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Add following DNS Entry {}", dnsEntry);
                            }
                            return dnsEntry;
                        }).collect(Collectors.toSet()));
            });
            dnsManager.createOrUpdateDnsEntries(dnsentries);
        })
                .match(BrickStateEvent.class, msg -> {
                    if (msg.getState() == BrickStateEvent.State.RUNNING ||
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

    private static DnsEntry.Type getDnsType(String host) {
        assert StringUtils.isNotBlank(host) : "host must be defined";
        Matcher matcher = IP_PATTERN.matcher(host);
        return matcher.matches() ? DnsEntry.Type.A : DnsEntry.Type.CNAME;
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
