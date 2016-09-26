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
package io.kodokojo.bdd.stage;


import com.google.gson.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.endpoint.dto.ProjectConfigDto;
import io.kodokojo.endpoint.dto.UserLightDto;
import io.kodokojo.model.Entity;
import io.kodokojo.model.User;
import io.kodokojo.service.repository.Repository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ApplicationThen<SELF extends ApplicationThen<?>> extends Stage<SELF> {


    @ExpectedScenarioState
    Repository repository;

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

    @ExpectedScenarioState
    HttpUserSupport httpUserSupport;

    private ProjectConfigDto projectConfigDto;

    public SELF it_exist_a_valid_user_with_username_$(@Quoted String username) {
        User user = repository.getUserByUsername(username);

        assertThat(user).isNotNull();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(user);
        Attachment attachment = Attachment.plainText(json).withTitle("User generate");
        currentStep.addAttachment(attachment);
        return self();
    }

    public SELF it_NOT_exist_a_valid_user_with_username_$(@Quoted String username) {
        User user = repository.getUserByUsername(username);

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

    public SELF user_$_belong_to_entity_of_project_configuration(@Quoted String username) {
        UserInfo requesterUserInfo = currentUsers.get(currentUserLogin);
        User user = repository.getUserByIdentifier(requesterUserInfo.getIdentifier());
        String entityIdOfCurrentUser = user.getEntityIdentifier();
        assertThat(entityIdOfCurrentUser).isNotNull();
        UserInfo userToValidate = currentUsers.get(username);
        user = repository.getUserByIdentifier(userToValidate.getIdentifier());
        String entityIdOfUserToValidate = user.getEntityIdentifier();
        assertThat(entityIdOfUserToValidate).isEqualTo(entityIdOfCurrentUser);
        return self();
    }


    public SELF it_exist_a_valid_project_configuration_in_store() {
        projectConfigDto = getProjectConfigurationFromCurrentProjectConfigId();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        currentStep.addAttachment(Attachment.plainText(gson.toJson(projectConfigDto)).withTitle("Project configuration").withFileName("projectConfiguration_" + projectConfigurationId + ".json"));
        return self();
    }

    public SELF it_exist_a_valid_project_configuration_in_store_which_contain_user(@Quoted String username) {
        try {
            Thread.sleep(500); // We badly wait actor done there jobs.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean found = lookupUsernameInProjectConfiguration(username);
        assertThat(found).isTrue();
        return self();
    }


    public SELF it_exist_NOT_a_valid_project_configuration_in_store_which_contain_user(@Quoted String usernameToNotFound) {
        try {
            Thread.sleep(500); // We badly wait actor done there jobs.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        Request request = HttpUserSupport.addBasicAuthentification(currentUser, builder).build();
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


    public SELF user_$_belong_to_entity_$(String username, String entityName) {
        UserInfo userInfo = currentUsers.get(username);
        User user = repository.getUserByIdentifier(userInfo.getIdentifier());
        String entityIdOfUserId = user.getEntityIdentifier();
        Entity entity = repository.getEntityById(entityIdOfUserId);
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(entityName);
        return self();
    }

    private boolean lookupUsernameInProjectConfiguration(@Quoted String username) {
        it_exist_a_valid_project_configuration_in_store();
        boolean found = false;
        Iterator<UserLightDto> iterator = projectConfigDto.getUsers().iterator();
        while (!found && iterator.hasNext()) {
            UserLightDto userLightDto = iterator.next();
            found = userLightDto.getUsername().equals(username);
        }
        return found;
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
        Request request = HttpUserSupport.addBasicAuthentification(requesterUserInfo, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);

            JsonParser parser = new JsonParser();
            String body = response.body().string();
            JsonObject json = (JsonObject) parser.parse(body);

            assertThat(json.getAsJsonPrimitive("name").getAsString()).isNotEmpty();
            if (complete) {
                assertThat(json.getAsJsonPrimitive("password").getAsString()).isNotEmpty();
                assertThat(json.getAsJsonPrimitive("email").getAsString()).isNotEmpty();
                assertThat(json.getAsJsonPrimitive("sshPublicKey").getAsString()).isNotEmpty();

            } else {
                assertThat(json.getAsJsonPrimitive("password").getAsString()).isEmpty();
            }
            if (StringUtils.isNotBlank(projectConfigurationId)) {
                assertThat(json.has("projectConfigurationIds"));
                JsonArray projectConfigurationIds = json.getAsJsonArray("projectConfigurationIds");
                assertThat(projectConfigurationIds).isNotEmpty();
                boolean foundCurrentProjectConfigurationId = false;
                Iterator<JsonElement> projectConfigIt = projectConfigurationIds.iterator();

                while (projectConfigIt.hasNext()) {
                    JsonObject projectConfig = (JsonObject) projectConfigIt.next();
                    assertThat(projectConfig.has("projectConfigurationId"));
                    foundCurrentProjectConfigurationId = projectConfigurationId.equals(projectConfig.getAsJsonPrimitive("projectConfigurationId").getAsString());
                }
                assertThat(foundCurrentProjectConfigurationId).isTrue();
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


    private ProjectConfigDto getProjectConfigurationFromCurrentProjectConfigId() {
        OkHttpClient httpClient = new OkHttpClient();
        UserInfo requesterUserInfo = currentUsers.get(currentUserLogin);
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig/" + projectConfigurationId).get();
        Request request = HttpUserSupport.addBasicAuthentification(requesterUserInfo, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);
            String bodyResponse = response.body().string();
            Gson gson = new GsonBuilder().create();
            ProjectConfigDto projectConfigDto = gson.fromJson(bodyResponse, ProjectConfigDto.class);
            assertThat(projectConfigDto).isNotNull();
            assertThat(projectConfigDto.getAdmins().get(0).getUsername()).isEqualTo(requesterUserInfo.getUsername());
            assertThat(projectConfigDto.getUsers()).isNotEmpty();
            assertThat(projectConfigDto.getStackConfigs()).isNotEmpty();

            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(bodyResponse);
            assertThat(json.has("name")).isTrue();
            assertThat(json.has("identifier")).isTrue();
            assertThat(json.has("admins")).isTrue();
            JsonArray admins = json.getAsJsonArray("admins");
            assertThat(admins).isNotEmpty();
            jsonUserAreValid(admins.iterator());
            JsonArray users = json.getAsJsonArray("users");
            jsonUserAreValid(users.iterator());
            assertThat(users).isNotEmpty();
            JsonArray stackConfigs = json.getAsJsonArray("stackConfigs");
            assertThat(stackConfigs).isNotEmpty();
            Iterator<JsonElement> stackConfigurationIt = stackConfigs.iterator();
            while (stackConfigurationIt.hasNext()) {
                JsonObject stackConf = (JsonObject) stackConfigurationIt.next();
                jsonStackConfigIsValid(stackConf);
            }

            return projectConfigDto;
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                closeQuietly(response.body());
            }
        }
        return null;
    }

    private void jsonStackConfigIsValid(JsonObject stackConf) {
        assertThat(stackConf.has("name")).isTrue();
        assertThat(stackConf.has("type")).isTrue();
        JsonArray brickConfigs = stackConf.getAsJsonArray("brickConfigs");
        assertThat(brickConfigs).isNotEmpty();
        Iterator<JsonElement> brickConfigIt = brickConfigs.iterator();
        while (brickConfigIt.hasNext()) {
            JsonObject brickConfig = (JsonObject) brickConfigIt.next();
            jsonBrickIsValid(brickConfig);
        }
    }

    private void jsonBrickIsValid(JsonObject brickConfig) {
        assertThat(brickConfig.has("name")).isTrue();
        assertThat(brickConfig.has("type")).isTrue();
    }

    private void jsonUserAreValid(Iterator<JsonElement> userIterator) {
        while (userIterator.hasNext()) {
            JsonObject user = (JsonObject) userIterator.next();
            assertThat(user.has("identifier"));
            assertThat(user.has("username"));
        }
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }

    private String getApiBaseUrl() {
        return getBaseUrl() + "/api/v1";
    }

}
