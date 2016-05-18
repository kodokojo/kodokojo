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
package io.kodokojo.bdd.stage.cluster;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.bdd.stage.UserInfo;
import io.kodokojo.bdd.stage.WebSocketConnectionResult;
import io.kodokojo.bdd.stage.WebSocketEventsListener;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.entrypoint.dto.BrickConfigDto;
import io.kodokojo.entrypoint.dto.StackConfigDto;
import io.kodokojo.model.Brick;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackType;
import io.kodokojo.model.User;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.store.ProjectStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class ClusterApplicationWhen<SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationWhen.class);

    @ExpectedScenarioState
    User currentUser;

    @ExpectedScenarioState
    ProjectManager projectManager;

    @ExpectedScenarioState
    ProjectStore projectStore;


    @ProvidedScenarioState
    ProjectConfiguration projectConfiguration;

    @ProvidedScenarioState
    String loadBalancerIp;

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    @ExpectedScenarioState
    WebSocketEventsListener webSocketEventsListener;

    @ProvidedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();

    public SELF i_start_a_default_project_with_name_$(String projectName) {

        String projectConfigurationId = StageUtils.createProjectConfiguration(getApiBaseUrl(), projectName, null, new UserInfo(currentUser));
        getProjectConfigurationData(projectConfigurationId);

        return self();
    }

    public SELF i_start_the_project() {

        OkHttpClient httpClient = new OkHttpClient();

        String url = "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/project/" + projectConfiguration.getIdentifier();
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Response response = null;

        try {
            response = httpClient.newCall(builder.build()).execute();
            assertThat(response.code()).isEqualTo(201);
            LOGGER.trace("Starting project");

            Map<String, Boolean> brickRunning = new HashMap<>();
            projectConfiguration.getDefaultBrickConfigurations().forEachRemaining(b -> {
                brickRunning.put(b.getName(), Boolean.FALSE);
            });

            JsonParser parser = new JsonParser();
            boolean allBrickStarted = true;

            long now = System.currentTimeMillis();
            long end = now + 180000;

            do {
                Iterator<String> messageReceive = webSocketEventsListener.getMessages().iterator();
                while(messageReceive.hasNext()) {
                    String message = messageReceive.next();
                    JsonObject root = (JsonObject) parser.parse(message);
                    if ("updateState".equals(root.getAsJsonPrimitive("action").getAsString())) {
                        JsonObject data = root.getAsJsonObject("data");
                        String brickName = data.getAsJsonPrimitive("brickName").getAsString();
                        String brickState = data.getAsJsonPrimitive("state").getAsString();
                        if ("RUNNING".equals(brickState)) {
                            brickRunning.put(brickName, Boolean.TRUE);
                        }
                    }
                }
                Iterator<Boolean> iterator = brickRunning.values().iterator();
                allBrickStarted = true;
                while (allBrickStarted && iterator.hasNext()) {
                    allBrickStarted = iterator.next();
                }
                if (!allBrickStarted) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                now = System.currentTimeMillis();
            } while (!allBrickStarted && now < end);
            assertThat(allBrickStarted).isTrue();
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        return self();
    }


    public SELF i_configure_a_project_with_name_$_and_only_brick_$(String projectName, String brickName) {

        Brick brick = new DefaultBrickFactory().createBrick(brickName);
        String brickType = brick.getType().name();
        BrickConfigDto brickConfig = new BrickConfigDto(brickName, brickType);
        StackConfigDto stackConfig = new StackConfigDto("build-A", StackType.BUILD.name(), Collections.singletonList(brickConfig));
        String projectConfigurationId = StageUtils.createProjectConfiguration(getApiBaseUrl(), projectName, stackConfig, new UserInfo(currentUser));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        getProjectConfigurationData(projectConfigurationId);

        return self();
    }

    private String getApiBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1";
    }

    private void getProjectConfigurationData(String projectConfigurationId) {

        this.projectConfiguration = projectStore.getProjectConfigurationById(projectConfigurationId);
        assertThat(projectConfigurationId).isNotEmpty();
        assertThat(this.projectConfiguration).isNotNull();

        loadBalancerIp = this.projectConfiguration.getDefaultStackConfiguration().getLoadBalancerIp();
    }

    public SELF i_create_a_new_user_$(@Quoted String email) {
        OkHttpClient httpClient = new OkHttpClient();

        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().post(emptyBody).url(getApiBaseUrl() +"/user");
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;
        String identifier = null;
        try {
            response = httpClient.newCall(request).execute();
            identifier = response.body().string();
            assertThat(identifier).isNotEmpty();
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), ("{\"email\": \"" + email + "\"}").getBytes());
        builder = new Request.Builder().post(body).url(getApiBaseUrl() + "/user/" +identifier);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        request = builder.build();
        response = null;
        try {
            response = httpClient.newCall(request).execute();
            JsonParser parser = new JsonParser();
            String bodyResponse = response.body().string();
            JsonObject json = (JsonObject) parser.parse(bodyResponse);
            String currentUsername = json.getAsJsonPrimitive("username").getAsString();
            String currentUserPassword = json.getAsJsonPrimitive("password").getAsString();
            String currentUserEmail = json.getAsJsonPrimitive("email").getAsString();
            String currentUserIdentifier = json.getAsJsonPrimitive("identifier").getAsString();
            currentUsers.put(currentUsername, new UserInfo(currentUsername, currentUserIdentifier, currentUserPassword, currentUserEmail));

        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        return self();
    }

    public SELF i_add_the_user_$_to_the_project(@Quoted String username) {

        UserInfo userInfo = currentUsers.get(username);
        assertThat(userInfo).isNotNull();

        OkHttpClient httpClient = new OkHttpClient();

        String json = "[\n" +
                "  \""+userInfo.getIdentifier()+"\"\n" +
                "]";

        RequestBody emptyBody = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        String url = getApiBaseUrl() +"/projectconfig/"+projectConfiguration.getIdentifier()+"/user";
        Request.Builder builder = new Request.Builder().put(emptyBody).url(url);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;

        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);
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
