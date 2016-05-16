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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.entrypoint.dto.ProjectCreationDto;
import io.kodokojo.model.User;
import org.apache.commons.io.IOUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class BrickStateNotificationWhen<SELF extends BrickStateNotificationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    String entryPointUrl;

    @ExpectedScenarioState
    User currentUser;

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
        ProjectCreationDto projectCreationDto = new ProjectCreationDto("123456", "Acme", currentUser.getIdentifier(), Collections.singletonList(currentUser.getIdentifier()));

        expectedBrickStarted = new String[]{"haproxy", "jenkins", "nexus", "gitlab"};
        startProjectConfiguration(projectCreationDto, expectedBrickStarted);
        return self();
    }

    private void startProjectConfiguration(ProjectCreationDto projectCreationDto, String[] expectedBrickStarted) {
        nbMessageExpected = new CountDownLatch((expectedBrickStarted.length * 3) + 1);
        WebSocketConnectionResult webSocketConnectionResult = StageUtils.connectToWebSocket(entryPointUrl, currentUser, nbMessageExpected);
        listener = webSocketConnectionResult.getListener();
        session = webSocketConnectionResult.getSession();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(projectCreationDto);

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request.Builder builder = new Request.Builder().url("http://" + entryPointUrl + "/api/v1/projectconfig").post(body);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            projectConfigurationIdentifier = response.body().string();

        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }


    public SELF i_start_the_project() {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().url("http://" + entryPointUrl + "/api/v1/project/" + projectConfigurationIdentifier).post(body);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return self();
    }
}
