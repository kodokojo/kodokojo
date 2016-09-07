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
package io.kodokojo.service;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.kodokojo.brick.*;
import io.kodokojo.model.*;
import io.kodokojo.model.Stack;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.repository.ProjectRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

@Deprecated
public class DefaultProjectManager implements ProjectManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProjectManager.class);

    //  Source Regexp http://sroze.io/2008/10/09/regex-ipv4-et-ipv6/
    private static final Pattern IP_PATTERN = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    private final String domain;

    private final ConfigurationStore configurationStore;

    private final ProjectRepository projectRepository;

    private final DnsManager dnsManager;

    private final BootstrapConfigurationProvider bootstrapConfigurationProvider;

    private final ActorSystem brickConfigurationStarter;

    private final BrickUrlFactory brickUrlFactory;

    private final BrickConfigurerProvider brickConfigurerProvider;

    @Inject
    public DefaultProjectManager(String domain,
                                 ConfigurationStore configurationStore,
                                 ProjectRepository projectRepository,
                                 BootstrapConfigurationProvider bootstrapConfigurationProvider,
                                 DnsManager dnsManager,
                                 BrickConfigurerProvider brickConfigurerProvider,
                                 ActorSystem brickConfigurationStarter,
                                 BrickUrlFactory brickUrlFactory) {

        if (isBlank(domain)) {
            throw new IllegalArgumentException("domain must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (bootstrapConfigurationProvider == null) {
            throw new IllegalArgumentException("bootstrapConfigurationProvider must be defined.");
        }
        if (brickConfigurationStarter == null) {
            throw new IllegalArgumentException("brickConfigurationStarter must be defined.");
        }
        if (dnsManager == null) {
            throw new IllegalArgumentException("dnsManager must be defined.");
        }
        if (brickConfigurerProvider == null) {
            throw new IllegalArgumentException("brickConfigurerProvider must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.brickConfigurationStarter = brickConfigurationStarter;
        this.domain = domain;
        this.configurationStore = configurationStore;
        this.projectRepository = projectRepository;
        this.bootstrapConfigurationProvider = bootstrapConfigurationProvider;
        this.brickConfigurerProvider = brickConfigurerProvider;
        this.dnsManager = dnsManager;
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public Project start(ProjectConfiguration projectConfiguration) throws ProjectAlreadyExistException {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (CollectionUtils.isEmpty(projectConfiguration.getStackConfigurations())) {
            throw new IllegalArgumentException("Unable to create a project without stack.");
        }

        String projectName = projectConfiguration.getName();

        Set<DnsEntry> dnsEntries = new HashSet<>();
        List<BrickStartContext> contexts = new ArrayList<>();
        Set<Stack> stacks = new HashSet<>();
        for (StackConfiguration stackConfiguration : projectConfiguration.getStackConfigurations()) {
            String lbHost = stackConfiguration.getLoadBalancerHost();
            DnsEntry.Type dnsType = getDnsType(lbHost);
            for (BrickConfiguration brickConfiguration : stackConfiguration.getBrickConfigurations()) {

                BrickType brickType = brickConfiguration.getType();
                if (brickType.isRequiredHttpExposed()) {
                    String brickDomainName = brickUrlFactory.forgeUrl(projectConfiguration, stackConfiguration.getName(), brickConfiguration);
                    dnsEntries.add(new DnsEntry(brickDomainName, dnsType, lbHost));
                }
                BrickStartContext context = new BrickStartContext(projectConfiguration, stackConfiguration, brickConfiguration);
                contexts.add(context);
            }

            Stack stack = new Stack(stackConfiguration.getName(), stackConfiguration.getType(), new HashSet<>());
            stacks.add(stack);

        }
        dnsManager.createOrUpdateDnsEntries(dnsEntries);
        Project project = new Project(projectConfiguration.getIdentifier(), projectName, new Date(), stacks);
        String projectId = projectRepository.addProject(project, projectConfiguration.getIdentifier());

        ActorRef endpointActor = brickConfigurationStarter.actorFor(EndpointActor.ACTOR_PATH);
        contexts.forEach(c -> {
            endpointActor.tell(c, ActorRef.noSender());
        });
        Project res = projectRepository.getProjectByIdentifier(projectId);
        return res;
    }

    @Override
    public void addUsersToProject(ProjectConfiguration projectConfiguration, List<User> usersToAdd) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (CollectionUtils.isEmpty(usersToAdd)) {
            throw new IllegalArgumentException("usersToAdd must be defined.");
        }

        projectConfiguration.getStackConfigurations().forEach(stackConfiguration -> {
            stackConfiguration.getBrickConfigurations().forEach(brickConfiguration -> {
                BrickConfigurer brickConfigurer = brickConfigurerProvider.provideFromBrick(brickConfiguration);
                String entrypoint = "https://" + brickUrlFactory.forgeUrl(projectConfiguration, stackConfiguration.getName(), brickConfiguration);
                BrickConfigurerData brickConfigurerData = new BrickConfigurerData(projectConfiguration.getName(),
                        stackConfiguration.getName(),
                        entrypoint,
                        domain,
                        IteratorUtils.toList(projectConfiguration.getAdmins()),
                        IteratorUtils.toList(projectConfiguration.getUsers())
                );
                brickConfigurerData.getContext().putAll(brickConfiguration.getProperties());
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Try to add users {} on entrypoint {}.", org.apache.commons.lang.StringUtils.join(usersToAdd, ","), entrypoint);
                    }
                    brickConfigurer.addUsers(brickConfigurerData, usersToAdd);
                } catch (BrickConfigurationException e) {
                    LOGGER.error("An error occure while add users to brick " + brickConfiguration.getName() + "[" + entrypoint + "] on project " + projectConfiguration.getName() + ".", e);
                }
            });
        });

    }

    private static DnsEntry.Type getDnsType(String host) {
        assert StringUtils.isNotBlank(host) : "host must be defined";
        Matcher matcher = IP_PATTERN.matcher(host);
        return matcher.matches() ? DnsEntry.Type.A : DnsEntry.Type.CNAME;
    }
}
