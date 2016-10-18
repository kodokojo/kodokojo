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
import io.kodokojo.model.*;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.actor.user.UserFetcherActor;
import io.kodokojo.service.repository.ProjectFetcher;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;
import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfigurationChangeUserActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    private ProjectConfiguration projectConfiguration;

    private Set<User> users;

    private ActorRef originalSender;

    private ProjectConfigurationChangeUserMsg originalMsg;

    private int nbBrickUpdateRequested;

    private int nbBrickUpdateResponse;

    public static Props PROPS(ProjectFetcher projectFetcher) {
        if (projectFetcher == null) {
            throw new IllegalArgumentException("projectFetcher must be defined.");
        }
        return Props.create(ProjectConfigurationChangeUserActor.class, projectFetcher);
    }

    public ProjectConfigurationChangeUserActor(ProjectFetcher projectFetcher) {
        receive(ReceiveBuilder.match(ProjectConfigurationChangeUserMsg.class, msg -> {
            this.originalMsg = msg;
            this.originalSender = sender();
            users = new HashSet<>();
            projectConfiguration = projectFetcher.getProjectConfigurationById(originalMsg.projectConfigurationId);
            if (projectConfiguration == null) {
                LOGGER.error("Unable to found an existing ProjectConfiguration with Identifiant = '{}'.", msg.projectConfigurationId);
            } else {
                msg.userIdentifiers.stream().forEach(userId -> {
                    getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new UserFetcherActor.UserFetchMsg(msg.getRequester(), userId), self());
                });
            }
        })
                .match(UserFetcherActor.UserFetchResultMsg.class, msg -> {
                    users = msg.getUsers();
                    if (CollectionUtils.isEmpty(users)) {
                        LOGGER.error("Unable to found a valid user with IDs '{}'.", StringUtils.join(msg.getUserIdRequeted(), ", "));
                    } else {
                        ProjectConfigurationBuilder builder = new ProjectConfigurationBuilder(projectConfiguration);
                        List<User> existingUsers = IteratorUtils.toList(projectConfiguration.getUsers());
                                List<String> userNames = users.stream().map(User::getUsername).collect(Collectors.toList());
                        switch (originalMsg.typeChange) {
                            case ADD:
                                existingUsers.addAll(users);
                                LOGGER.debug("Adding {} to projectConfiguration '{}'.", StringUtils.join(userNames, ","), projectConfiguration.getName());
                                break;
                            case REMOVE:
                                existingUsers.removeAll(users);
                                LOGGER.debug("Remove {} to projectConfiguration '{}'.", StringUtils.join(userNames, ","), projectConfiguration.getName());
                                break;
                        }
                        builder.setUsers(existingUsers);
                        getContext().actorFor(EndpointActor.ACTOR_PATH).tell(new ProjectConfigurationUpdaterActor.ProjectConfigurationUpdaterMsg(originalMsg.getRequester(), builder.build()), self());
                    }
                })
                .match(ProjectConfigurationUpdaterActor.ProjectConfigurationUpdaterResultMsg.class, msg -> {
                    projectConfiguration = msg.getProjectConfiguration();
                    Project project = projectFetcher.getProjectByProjectConfigurationId(projectConfiguration.getIdentifier());
                    if (project == null) {
                        LOGGER.debug("ProjectConfiguration '{}' don't have currently a running project.", projectConfiguration.getName());
                        originalSender.tell(new ProjectConfigurationChangeUserResultMsg(true), self());
                        getContext().stop(self());
                    } else {
                        nbBrickUpdateRequested = 0;
                        nbBrickUpdateResponse = 0;

                        List<UpdateData<User>> updateDataUsers = new ArrayList<>();
                        if (originalMsg.typeChange == TypeChange.ADD) {
                            updateDataUsers.addAll(users.stream().map(u -> new UpdateData<>(null, u)).collect(Collectors.toSet()));
                        } else  if (originalMsg.typeChange == TypeChange.REMOVE) {
                            updateDataUsers.addAll(users.stream().map(u -> new UpdateData<>(u, null)).collect(Collectors.toSet()));
                        }

                        ActorRef endpoint = getContext().actorFor(EndpointActor.ACTOR_PATH);

                        projectConfiguration.getStackConfigurations().stream().forEach(s -> {
                            s.getBrickConfigurations().stream().forEach(b -> {

                                BrickUpdateUserActor.BrickUpdateUserMsg msgUpdate = new BrickUpdateUserActor.BrickUpdateUserMsg(originalMsg.typeChange, updateDataUsers, projectConfiguration, s, b);
                                nbBrickUpdateRequested++;
                                endpoint.tell(msgUpdate, self());
                            });
                        });
                        LOGGER.debug("Request add user {} on project {} for {} bricks.", StringUtils.join(users.stream().map(User::getUsername).collect(Collectors.toList()), ", "), projectConfiguration.getName(), nbBrickUpdateRequested);
                    }
                })
                .match(BrickUpdateUserActor.BrickUpdateUserResultMsg.class, msg -> {
                    nbBrickUpdateResponse++;
                    LOGGER.debug("Receive {}/{} brick update user result message.", nbBrickUpdateResponse, nbBrickUpdateRequested);
                    if (nbBrickUpdateRequested == nbBrickUpdateResponse) {
                        originalSender.tell(new ProjectConfigurationChangeUserResultMsg(true), self());
                        getContext().stop(self());
                    }
                })
                .matchAny(this::unhandled).build());
    }


    public static class ProjectConfigurationChangeUserMsg extends UserRequestMessage {


        private final TypeChange typeChange;

        private final String projectConfigurationId;

        private final List<String> userIdentifiers;

        public ProjectConfigurationChangeUserMsg(User requester, TypeChange typeChange, String projectConfigurationId, List<String> userIdentifiers) {
            super(requester);
            if (typeChange == null) {
                throw new IllegalArgumentException("typeChange must be defined.");
            }
            if (isBlank(projectConfigurationId)) {
                throw new IllegalArgumentException("projectConfigurationId must be defined.");
            }
            if (userIdentifiers == null) {
                throw new IllegalArgumentException("userIdentifiers must be defined.");
            }
            this.typeChange = typeChange;
            this.projectConfigurationId = projectConfigurationId;
            this.userIdentifiers = userIdentifiers;
        }

    }

    public static class ProjectConfigurationChangeUserResultMsg {

        private final boolean success;

        public ProjectConfigurationChangeUserResultMsg(boolean success) {
            this.success = success;
        }
    }

}
