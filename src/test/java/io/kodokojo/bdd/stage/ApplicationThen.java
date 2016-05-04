package io.kodokojo.bdd.stage;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.entrypoint.dto.ProjectConfigDto;
import io.kodokojo.entrypoint.dto.UserDto;
import io.kodokojo.model.User;
import io.kodokojo.service.redis.RedisUserManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ApplicationThen<SELF extends ApplicationThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    RedisUserManager userManager;

    @ExpectedScenarioState
    CurrentStep currentStep;

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    String projectConfigurationId;

    @ExpectedScenarioState
    int restEntryPointPort;

    @ExpectedScenarioState
    String currentUserLogin;

    @ExpectedScenarioState
    Map<String, UserInfo> currentUsers;

    private ProjectConfigDto projectConfigDto;

    public SELF it_exist_a_valid_user_with_username_$(@Quoted String username) {
        User user = userManager.getUserByUsername(username);

        assertThat(user).isNotNull();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(user);
        Attachment attachment = Attachment.plainText(json).withTitle("User generate");
        currentStep.addAttachment(attachment);
        return self();
    }

    public SELF it_NOT_exist_a_valid_user_with_username_$(@Quoted String username) {
        User user = userManager.getUserByUsername(username);

        assertThat(user).isNull();
        return self();
    }

    public SELF it_is_possible_to_get_details_for_user_$(@Quoted String username) {
        getUserDetails(username, false);
        return self();
    }

    public SELF it_is_possible_to_get_complete_details_for_user_$(@Quoted String username) {
        getUserDetails(username, true);
        return self();
    }

    public SELF it_is_NOT_possible_to_get_complete_details_for_user_$(@Quoted String username) {
        getUserDetails(username, false);
        return self();
    }

    private void getUserDetails(String username, boolean complete) {
        OkHttpClient httpClient = new OkHttpClient();
        UserInfo requesterUserInfo = currentUsers.get(currentUserLogin);
        UserInfo targetUserInfo = currentUsers.get(username);
        String url = getApiBaseUrl() + "/user";
        if (!requesterUserInfo.getIdentifier().equals(targetUserInfo.getIdentifier())) {
            url += "/" + targetUserInfo.getIdentifier();
        }
        Request.Builder builder = new Request.Builder().get().url(url);
        Request request = StageUtils.addBasicAuthentification(requesterUserInfo, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);

            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(response.body().string());

            assertThat(json.getAsJsonPrimitive("name").getAsString()).isNotEmpty();
            if (complete) {
                assertThat(json.getAsJsonPrimitive("password").getAsString()).isNotEmpty();
                assertThat(json.getAsJsonPrimitive("email").getAsString()).isNotEmpty();
                assertThat(json.getAsJsonPrimitive("sshPublicKey").getAsString()).isNotEmpty();
            } else {
                assertThat(json.getAsJsonPrimitive("password").getAsString()).isEmpty();
                assertThat(json.getAsJsonPrimitive("email").getAsString()).isEmpty();
                assertThat(json.getAsJsonPrimitive("sshPublicKey").getAsString()).isEmpty();
            }
        } catch (IOException e) {
            fail("Unable to get User details on Url " + url, e);
        } finally {
            if (response != null) {
                try {
                    response.body().close();
                } catch (IOException e) {
                    fail("Fail to close Http response.", e);
                }
            }
        }
    }

    public SELF it_exist_a_valid_project_configuration_in_store() {
        OkHttpClient httpClient = new OkHttpClient();
        UserInfo requesterUserInfo = currentUsers.get(currentUserLogin);
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig/" + projectConfigurationId).get();
        Request request = StageUtils.addBasicAuthentification(requesterUserInfo, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);
            String bodyResponse = response.body().string();
            Gson gson = new GsonBuilder().create();
            projectConfigDto = gson.fromJson(bodyResponse, ProjectConfigDto.class);
            assertThat(projectConfigDto).isNotNull();
            assertThat(projectConfigDto.getAdmins().get(0).getUsername()).isEqualTo(requesterUserInfo.getUsername());
            assertThat(projectConfigDto.getUsers()).isNotEmpty();
            assertThat(projectConfigDto.getStackConfigs()).isNotEmpty();
            currentStep.addAttachment(Attachment.plainText(bodyResponse).withTitle("Project configuration").withFileName("projectConfiguration_" + projectConfigurationId + ".json"));
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                closeQuietly(response.body());
            }
        }
        return self();
    }

    public SELF it_exist_a_valid_project_configuration_in_store_which_contain_user(@Quoted String username) {
        boolean found = lookupUsernameInProjectConfiguration(username);
        assertThat(found).isTrue();
        return self();
    }


    public SELF it_exist_NOT_a_valid_project_configuration_in_store_which_contain_user(@Quoted String usernameToNotFound) {
        boolean found = lookupUsernameInProjectConfiguration(usernameToNotFound);
        assertThat(found).isFalse();
        return self();
    }

    public SELF add_user_$_to_project_configuration_as_user_$_will_fail(@Quoted String usernameToAdd, @Quoted String usernameOwner) {
        UserInfo currentUser = currentUsers.get(usernameOwner);
        UserInfo userToAdd = currentUsers.get(usernameToAdd);

        String json = "[\n" +
                "    \"" + userToAdd.getIdentifier() + "\"\n" +
                "  ]";

        OkHttpClient httpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(com.squareup.okhttp.MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig/" + projectConfigurationId + "/user").put(body);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(403);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return self();
    }

    private boolean lookupUsernameInProjectConfiguration(@Quoted String username) {
        it_exist_a_valid_project_configuration_in_store();
        boolean found = false;
        Iterator<UserDto> iterator = projectConfigDto.getUsers().iterator();
        while (!found && iterator.hasNext()) {
            UserDto userDto = iterator.next();
            found = userDto.getUsername().equals(username);
        }
        return found;
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }

    private String getApiBaseUrl() {
        return getBaseUrl() + "/api/v1";
    }

}
