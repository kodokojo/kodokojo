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
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.UpdateData;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.repository.ProjectRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public class ListAndUpdateUserToProjectActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    private final ProjectRepository projectRepository;

    private ActorRef originalSender;

    private ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg initialMsg;

    private int nbBrickReceived = 0;

    private int nbBrickExpected = 0;

    public ListAndUpdateUserToProjectActor(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        receive(ReceiveBuilder
                .match(ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg.class, this::onListAndUpdateUser)
                .match(BrickUpdateUserActor.BrickUpdateUserResultMsg.class, this::onBrickUpdateUserResponse)
                .matchAny(this::unhandled).build());
    }

    static Props PROPS(ProjectRepository projectRepository) {
        requireNonNull(projectRepository, "projectRepository must be defined.");

        return Props.create(ListAndUpdateUserToProjectActor.class, projectRepository);
    }

    private void onListAndUpdateUser(ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg msg) {

        originalSender = sender();
        initialMsg = msg;

        List<UpdateData<User>> userList = Collections.singletonList(msg.getUser());

        ActorRef endpoint = getContext().actorFor(EndpointActor.ACTOR_PATH);

        Set<String> projectConfigIds = projectRepository.getProjectConfigIdsByUserIdentifier(msg.getUser().getOldData().getIdentifier());
        projectConfigIds.stream().forEach(projectConfigId -> {
            ProjectConfiguration projectConfiguration = projectRepository.getProjectConfigurationById(projectConfigId);
            projectConfiguration.getStackConfigurations().stream().forEach(stackConfiguration -> {
                stackConfiguration.getBrickConfigurations().stream().forEach(brickConfiguration -> {
                    BrickUpdateUserActor.BrickUpdateUserMsg brickUpdateUserMsg = new BrickUpdateUserActor.BrickUpdateUserMsg(TypeChange.UPDATE, userList, projectConfiguration, stackConfiguration, brickConfiguration);
                    endpoint.tell(brickUpdateUserMsg, self());
                    nbBrickExpected++;
                });
            });
        });
    }

    private void onBrickUpdateUserResponse(BrickUpdateUserActor.BrickUpdateUserResultMsg msg) {
        if (msg.isSuccess()) {
            nbBrickReceived++;
            if (nbBrickReceived == nbBrickExpected) {
                ProjectUpdaterMessages.ListAndUpdateUserToProjectResultMsg response = new ProjectUpdaterMessages.ListAndUpdateUserToProjectResultMsg(initialMsg.getRequester(), initialMsg, true);
                originalSender.tell(response, self());
                getContext().stop(self());
            }
        } else {
            ProjectUpdaterMessages.ListAndUpdateUserToProjectResultMsg response = new ProjectUpdaterMessages.ListAndUpdateUserToProjectResultMsg(initialMsg.getRequester(), initialMsg, false);
            originalSender.tell(response, self());
            getContext().stop(self());
        }
    }


}
