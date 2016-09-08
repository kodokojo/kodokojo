/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserRepository;

import static akka.event.Logging.getLogger;

public class ProjectEndpointActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(ProjectRepository projectRepository, UserRepository userRepository , ProjectManager projectManager, DnsManager dnsManager,  BrickFactory brickFactory, BrickManager brickManager, BootstrapConfigurationProvider bootstrapConfigurationProvider, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (projectManager == null) {
            throw new IllegalArgumentException("projectManager must be defined.");
        }
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }
        if (brickManager == null) {
            throw new IllegalArgumentException("brickManager must be defined.");
        }
        if (dnsManager == null) {
            throw new IllegalArgumentException("dnsManager must be defined.");
        }
        if (bootstrapConfigurationProvider == null) {
            throw new IllegalArgumentException("bootstrapConfigurationProvider must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        if (sslCertificatProvider == null) {
            throw new IllegalArgumentException("sslCertificatProvider must be defined.");
        }
        return Props.create(ProjectEndpointActor.class, projectRepository, userRepository, projectManager, dnsManager, brickFactory, brickManager, bootstrapConfigurationProvider, configurationStore, brickUrlFactory, sslCertificatProvider);
    }

    public static final String NAME = "projectEndpointProps";

    public ProjectEndpointActor(ProjectRepository projectRepository, UserRepository userRepository,  ProjectManager projectManager, DnsManager dnsManager ,BrickFactory brickFactory, BrickManager brickManager, BootstrapConfigurationProvider bootstrapConfigurationProvider, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (projectManager == null) {
            throw new IllegalArgumentException("projectManager must be defined.");
        }
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }

        receive(ReceiveBuilder
                .match(ProjectConfigurationBuilderActor.ProjectConfigurationBuildMsg.class, msg -> {
                    LOGGER.debug("Forward building of ProjectConfiguration to ProjectConfigurationBuilderActor.");
                    getContext().actorOf(ProjectConfigurationBuilderActor.PROPS(brickFactory)).forward(msg, getContext());

                })
                .match(ProjectConfigurationDtoCreatorActor.ProjectConfigurationDtoCreateMsg.class, msg -> {
                    getContext().actorOf(ProjectConfigurationDtoCreatorActor.PROPS(projectRepository)).forward(msg, getContext());
                })
                .match(BootstrapStackActor.BootstrapStackMsg.class, msg -> {
                    LOGGER.debug("Bootstrapping a project stack.");
                    getContext().actorOf(BootstrapStackActor.PROPS(bootstrapConfigurationProvider, configurationStore)).forward(msg, getContext());
                })
                .match(BrickStartContext.class, msg -> {
                    getContext().actorOf(BrickConfigurationStarterActor.PROPS(brickManager, configurationStore, brickUrlFactory, sslCertificatProvider)).forward(msg, getContext());
                })
                .match(ProjectUpdaterActor.ProjectUpdateMsg.class, msg -> {
                    getContext().actorOf(ProjectUpdaterActor.PROPS(projectRepository)).forward(msg, getContext());
                })
                .match(ProjectCreatorActor.ProjectCreateMsg.class, msg -> {
                    getContext().actorOf(ProjectCreatorActor.PROPS(projectRepository)).forward(msg, getContext());
                }).match(ProjectCreatorActor.ProjectCreateMsg.class, msg -> {
                    getContext().actorOf(ProjectCreatorActor.PROPS(projectRepository)).forward(msg, getContext());
                }).match(ProjectConfigurationStarterActor.ProjectConfigurationStartMsg.class, msg -> {
                    getContext().actorOf(ProjectConfigurationStarterActor.PROPS(projectRepository)).forward(msg, getContext());
                }).match(ProjectConfigurationUpdaterActor.ProjectConfigurationUpdaterMsg.class, msg -> {
                    getContext().actorOf(ProjectConfigurationUpdaterActor.PROPS(projectRepository)).forward(msg, getContext());
                }).match(StackConfigurationStarterActor.StackConfigurationStartMsg.class, msg -> {
                    getContext().actorOf(StackConfigurationStarterActor.PROPS(dnsManager, brickUrlFactory)).forward(msg, getContext());
                })
                .matchAny(this::unhandled).build());
    }


}
