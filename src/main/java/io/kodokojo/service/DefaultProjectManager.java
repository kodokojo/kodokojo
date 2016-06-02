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


import io.kodokojo.brick.*;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.model.Stack;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.store.ProjectStore;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultProjectManager implements ProjectManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProjectManager.class);

    private final String domain;

    private final ConfigurationStore configurationStore;

    private final ProjectStore projectStore;

    private final DnsManager dnsManager;

    private final BootstrapConfigurationProvider bootstrapConfigurationProvider;

    private final BrickConfigurationStarter brickConfigurationStarter;

    private final BrickUrlFactory brickUrlFactory;

    private final BrickConfigurerProvider brickConfigurerProvider;

    @Inject
    public DefaultProjectManager(String domain,
                                 ConfigurationStore configurationStore,
                                 ProjectStore projectStore,
                                 BootstrapConfigurationProvider bootstrapConfigurationProvider,
                                 DnsManager dnsManager,
                                 BrickConfigurerProvider brickConfigurerProvider,
                                 BrickConfigurationStarter brickConfigurationStarter,
                                 BrickUrlFactory brickUrlFactory) {

        if (isBlank(domain)) {
            throw new IllegalArgumentException("domain must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        if (projectStore == null) {
            throw new IllegalArgumentException("projectStore must be defined.");
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
        this.projectStore = projectStore;
        this.bootstrapConfigurationProvider = bootstrapConfigurationProvider;
        this.brickConfigurerProvider = brickConfigurerProvider;
        this.dnsManager = dnsManager;
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public BootstrapStackData bootstrapStack(String projectName, String stackName, StackType stackType) {
        if (!projectStore.projectNameIsValid(projectName)) {
            throw new IllegalArgumentException("project name " + projectName + " isn't valid.");
        }
        String loadBalancerIp = bootstrapConfigurationProvider.provideLoadBalancerIp(projectName, stackName);
        int sshPortEntrypoint = 0;
        if (stackType == StackType.BUILD) {
            sshPortEntrypoint = bootstrapConfigurationProvider.provideSshPortEntrypoint(projectName, stackName);
        }
        BootstrapStackData res = new BootstrapStackData(projectName, stackName, loadBalancerIp, sshPortEntrypoint);
        configurationStore.storeBootstrapStackData(res);
        return res;
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
            String lbIp = stackConfiguration.getLoadBalancerIp();

            for (BrickConfiguration brickConfiguration : stackConfiguration.getBrickConfigurations()) {
                Brick brick = brickConfiguration.getBrick();
                BrickType brickType = brick.getType();
                if (brickType.isRequiredHttpExposed()) {
                    String brickDomainName = brickUrlFactory.forgeUrl(projectConfiguration, stackConfiguration.getName(), brickConfiguration);
                    dnsEntries.add(new DnsEntry(brickDomainName, DnsEntry.Type.A, lbIp));
                }
                BrickStartContext context = new BrickStartContext(projectConfiguration, stackConfiguration, brickConfiguration, domain, lbIp);
                contexts.add(context);
            }

            Stack stack = new Stack(stackConfiguration.getName(), stackConfiguration.getType(), new HashSet<>());
            stacks.add(stack);

        }
        dnsManager.createOrUpdateDnsEntries(dnsEntries);
        contexts.forEach(brickConfigurationStarter::start);
        Project project = new Project(projectConfiguration.getIdentifier(), projectName, new Date(), stacks);
        return project;
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
                BrickConfigurer brickConfigurer = brickConfigurerProvider.provideFromBrick(brickConfiguration.getBrick());
                String entrypoint = "http://" + brickUrlFactory.forgeUrl(projectConfiguration, stackConfiguration.getName(), brickConfiguration);
                BrickConfigurerData brickConfigurerData = new BrickConfigurerData(projectConfiguration.getName(),
                        stackConfiguration.getName(),
                        entrypoint,
                        domain,
                        IteratorUtils.toList(projectConfiguration.getAdmins()),
                        IteratorUtils.toList(projectConfiguration.getUsers())
                );
                brickConfigurerData.getContext().putAll(brickConfiguration.getCustomData());
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
}
