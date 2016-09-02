package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.model.PortDefinition;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;

public class StackConfigurationStarterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    //  Source Regexp http://sroze.io/2008/10/09/regex-ipv4-et-ipv6/
    private static final Pattern IP_PATTERN = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");


    public static Props PROPS(DnsManager dnsManager, BrickUrlFactory brickUrlFactory) {
        if (dnsManager == null) {
            throw new IllegalArgumentException("dnsManager must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        return Props.create(StackConfigurationStarterActor.class, dnsManager, brickUrlFactory);
    }

    private StackConfigurationStartMsg intialMsg;

    private ActorRef originalSender;

    private int nbResponse = 0;

    public StackConfigurationStarterActor(DnsManager dnsManager, BrickUrlFactory brickUrlFactory) {
        receive(ReceiveBuilder.match(StackConfigurationStartMsg.class, msg -> {
            intialMsg = msg;
            originalSender = sender();
            ActorRef endpointActor = getContext().actorFor(EndpointActor.ACTOR_PATH);

            Set<DnsEntry> dnsentries = new HashSet<>();
            msg.stackConfiguration.getBrickConfigurations().forEach(b -> {
                endpointActor.tell(new BrickStartContext(msg.projectConfiguration, msg.stackConfiguration, b),  self());
                dnsentries.addAll(b.getPortDefinitions().stream()
                        .map(p -> {
                            String entry = brickUrlFactory.forgeUrl(msg.projectConfiguration, msg.stackConfiguration.getName(), b);
                            DnsEntry dnsEntry = new DnsEntry(entry, getDnsType(msg.stackConfiguration.getLoadBalancerHost()), msg.stackConfiguration.getLoadBalancerHost());
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Add following DNS Entry {}", dnsEntry);
                            }
                            return dnsEntry;
                        }).collect(Collectors.toSet()));
            });
            dnsManager.createOrUpdateDnsEntries(dnsentries);
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
