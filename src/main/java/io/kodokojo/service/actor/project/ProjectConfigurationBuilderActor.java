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
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.endpoint.dto.BrickConfigDto;
import io.kodokojo.endpoint.dto.ProjectCreationDto;
import io.kodokojo.endpoint.dto.StackConfigDto;
import io.kodokojo.model.*;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.user.UserFetcherActor;
import io.kodokojo.service.actor.user.UserServiceCreatorActor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;

public class ProjectConfigurationBuilderActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(BrickFactory brickFactory) {
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }
        return Props.create(ProjectConfigurationBuilderActor.class, brickFactory);
    }

    private final BrickFactory brickFactory;

    private ActorRef originalSender;

    private ProjectConfigurationBuildMsg initialMsg;

    private UserService userService;

    private Set<User> admins;

    private Set<User> users;

    private String loadBalancerHost;

    private int scmSshPort = 0;

    private List<StackConfigDto> stackConfigDtos;

    public ProjectConfigurationBuilderActor(BrickFactory brickFactory) {
        this.brickFactory = brickFactory;
        receive(ReceiveBuilder.match(ProjectConfigurationBuildMsg.class, msg -> {
            originalSender = sender();
            initialMsg = msg;
            ProjectCreationDto projectCreationDto = msg.getProjectCreationDto();

            Set<String> userRequested = new HashSet<>();
            userRequested.add(projectCreationDto.getOwnerIdentifier());
            if (CollectionUtils.isNotEmpty(projectCreationDto.getUserIdentifiers())) {
                userRequested.addAll(projectCreationDto.getUserIdentifiers());
            }

            ActorSelection akkaEndpoint = getContext().actorSelection(EndpointActor.ACTOR_PATH);
            akkaEndpoint.tell(new UserFetcherActor.UserFetchMsg(msg.getRequester(), userRequested), self());
            akkaEndpoint.tell(new UserServiceCreatorActor.UserServiceCreateMsg(msg.getRequester(), msg.getProjectCreationDto().getName() + "-service"), self());

            stackConfigDtos = CollectionUtils.isEmpty(projectCreationDto.getStackConfigs()) ? new ArrayList<>() : projectCreationDto.getStackConfigs();
            if (CollectionUtils.isEmpty(stackConfigDtos)) {
                List<BrickConfigDto> brickDtos = new ArrayList<>();
                addBrick(DefaultBrickFactory.JENKINS, brickDtos);
                addBrick(DefaultBrickFactory.GITLAB, brickDtos);
                addBrick(DefaultBrickFactory.NEXUS, brickDtos);
                StackConfigDto stackConfigDto = new StackConfigDto("build-A", StackType.BUILD.name(), brickDtos);
                stackConfigDtos.add(stackConfigDto);
            }

            StackConfigDto defaultStackConfigDtos = stackConfigDtos.get(0);
            BootstrapStackActor.BootstrapStackMsg bootstrapStackMsg = new BootstrapStackActor.BootstrapStackMsg(projectCreationDto.getName(), defaultStackConfigDtos.getName(), StackType.valueOf(defaultStackConfigDtos.getType()));

            akkaEndpoint.tell(bootstrapStackMsg, self());
        }).match(UserFetcherActor.UserFetchResultMsg.class, msg -> {
            admins = msg.getUsers().stream()
                    .filter(filterByUserIds(Collections.singleton(initialMsg.getProjectCreationDto().getOwnerIdentifier())))
                    .collect(Collectors.toSet());
            users = msg.getUsers().stream()
                    .filter(filterByUserIds(initialMsg.getProjectCreationDto().getUserIdentifiers()))
                    .collect(Collectors.toSet());
            users.addAll(admins);
            tryToBuild();
        }).match(UserServiceCreatorActor.UserServiceCreateResultMsg.class, msg -> {
            userService = msg.getUserService();
            tryToBuild();
        })
                .match(BootstrapStackActor.BootstrapStackResultMsg.class, msg -> {

                    BootstrapStackData bootstrapStackData = msg.getBootstrapStackData();
                    loadBalancerHost = bootstrapStackData.getLoadBalancerHost();
                    scmSshPort = bootstrapStackData.getSshPort();
                    tryToBuild();
                })
                .matchAny(this::unhandled).build());
    }

    private static Predicate<User> filterByUserIds(Collection<String> userIds) {
        return user -> user != null && CollectionUtils.isNotEmpty(userIds) && userIds.contains(user.getIdentifier());
    }

    private void tryToBuild() {

        if (CollectionUtils.isNotEmpty(admins) &&
                StringUtils.isNotBlank(loadBalancerHost) &&
                scmSshPort > 0 &&
                userService != null) {

            User requester = initialMsg.getRequester();
            ProjectCreationDto projectCreationDto = initialMsg.getProjectCreationDto();

            Set<StackConfiguration> stackConfiguration = stackConfigDtos.stream().map(stackConfigDto -> {
                Set<BrickConfiguration> brickConfigurations = stackConfigDto.getBrickConfigs().stream()
                        .map(brickConfigDto -> brickFactory.createBrick(brickConfigDto.getName()))
                        .collect(Collectors.toSet());
                return new StackConfiguration(stackConfigDto.getName(), StackType.valueOf(stackConfigDto.getType()), brickConfigurations, loadBalancerHost, scmSshPort);
            }).collect(Collectors.toSet());


            ProjectConfiguration projectConfiguration = new ProjectConfiguration(requester.getEntityIdentifier(), projectCreationDto.getName(), userService, new ArrayList<>(admins), stackConfiguration, new ArrayList<>(users));
            originalSender.tell(new ProjectConfigurationBuildResultMsg(initialMsg.getRequester(), projectConfiguration), self());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Return a built ProjectConfiguration for project {}.", initialMsg.getProjectCreationDto().getName());
                LOGGER.debug("Loadbalancer host {}", loadBalancerHost);
                LOGGER.debug("sshPort {}", scmSshPort);
            }

        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Not yet ready to build Project configuration for project {}.", initialMsg.getProjectCreationDto().getName());
        }
    }

    private void addBrick(String name, List<BrickConfigDto> brickConfigDtos) {
        BrickConfiguration brickConfiguration = brickFactory.createBrick(name);
        brickConfigDtos.add(new BrickConfigDto(brickConfiguration.getName(), brickConfiguration.getType().toString(), brickConfiguration.getVersion()));
    }

    public static class ProjectConfigurationBuildMsg extends UserRequestMessage {

        private final ProjectCreationDto projectCreationDto;

        public ProjectConfigurationBuildMsg(User requester, ProjectCreationDto projectCreationDto) {
            super(requester);
            if (projectCreationDto == null) {
                throw new IllegalArgumentException("projectCreationDto must be defined.");
            }
            this.projectCreationDto = projectCreationDto;
        }

        public ProjectCreationDto getProjectCreationDto() {
            return projectCreationDto;
        }
    }

    public static class ProjectConfigurationBuildResultMsg extends UserRequestMessage {

        private final ProjectConfiguration projectConfiguration;

        public ProjectConfigurationBuildResultMsg(User requester, ProjectConfiguration projectConfiguration) {
            super(requester);
            this.projectConfiguration = projectConfiguration;
        }

        public ProjectConfiguration getProjectConfiguration() {
            return projectConfiguration;
        }
    }

}
