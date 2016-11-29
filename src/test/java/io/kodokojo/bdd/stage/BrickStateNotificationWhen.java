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
package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.commons.endpoint.dto.ProjectCreationDto;

import javax.websocket.Session;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class BrickStateNotificationWhen<SELF extends BrickStateNotificationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    HttpUserSupport httpUserSupport;

    @ExpectedScenarioState
    UserInfo currentUser;

    @ProvidedScenarioState
    WebSocketEventsListener listener;

    @ProvidedScenarioState
    Session session;

    @ProvidedScenarioState
    String[] expectedBrickStarted;

    @ProvidedScenarioState
    CountDownLatch nbMessageExpected;

    @ProvidedScenarioState
    String projectConfigurationIdentifier;

    public SELF i_create_a_project_configuration_with_default_brick() {
        ProjectCreationDto projectCreationDto = new ProjectCreationDto("123456", "Acme", currentUser.getIdentifier(),null, Collections.singletonList(currentUser.getIdentifier()));

        expectedBrickStarted = new String[]{"jenkins", "nexus", "gitlab"};
        nbMessageExpected = new CountDownLatch((expectedBrickStarted.length * 3) + 1);
        WebSocketConnectionResult webSocketConnectionResult = httpUserSupport.connectToWebSocketAndWaitMessage(currentUser, nbMessageExpected);
        listener = webSocketConnectionResult.getListener();
        session = webSocketConnectionResult.getSession();

        projectConfigurationIdentifier = httpUserSupport.createProjectConfiguration(projectCreationDto.getName(), null , currentUser);
        return self();
    }

    public SELF i_start_the_project() {
        httpUserSupport.startProject(projectConfigurationIdentifier,currentUser);
        return self();
    }
}
