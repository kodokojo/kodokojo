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
package io.kodokojo.bdd.stage.cluster;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.bdd.stage.HttpUserSupport;
import io.kodokojo.bdd.stage.UserInfo;
import io.kodokojo.bdd.stage.WebSocketEventsListener;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.endpoint.dto.BrickConfigDto;
import io.kodokojo.endpoint.dto.StackConfigDto;
import io.kodokojo.model.Brick;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class ClusterApplicationWhen<SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationWhen.class);

    @ExpectedScenarioState
    UserInfo currentUser;

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

    @ProvidedScenarioState
    HttpUserSupport httpUserSupport;

    public SELF i_start_a_default_project_with_name_$(String projectName) {

        String projectConfigurationId = httpUserSupport.createProjectConfiguration(projectName, null, currentUser);
        getProjectConfigurationData(projectConfigurationId);

        return self();
    }

    public SELF i_start_the_project() {

        OkHttpClient httpClient = new OkHttpClient();

        String url = "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/project/" + projectConfiguration.getIdentifier();
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        builder = HttpUserSupport.addBasicAuthentification(currentUser, builder);
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
                while (messageReceive.hasNext()) {
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
        BrickConfigDto brickConfig = new BrickConfigDto(brickName, brickType, brick.getVersion());
        StackConfigDto stackConfig = new StackConfigDto("build-A", StackType.BUILD.name(), Collections.singletonList(brickConfig));
        String projectConfigurationId = httpUserSupport.createProjectConfiguration(projectName, stackConfig, currentUser);
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
        UserInfo user = httpUserSupport.createUser(currentUser, email);
        currentUsers.put(user.getUsername(), user);
        return self();
    }

    public SELF i_add_the_user_$_to_the_project(@Quoted String username) {
        UserInfo userInfo = currentUsers.get(username);
        assertThat(userInfo).isNotNull();
        httpUserSupport.addUserToProjectConfiguration(projectConfiguration.getIdentifier(), currentUser, userInfo);
        return self();
    }
}
