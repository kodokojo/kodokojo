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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.service.ProjectAlreadyExistException;
import io.kodokojo.service.ProjectManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ApplicationWhen<SELF extends ApplicationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    @ProvidedScenarioState
    String newUserId;

    @ProvidedScenarioState
    String projectConfigurationId;

    @ExpectedScenarioState
    ProjectManager projectManager;

    @ExpectedScenarioState
    Map<String, UserInfo> currentUsers;

    @ExpectedScenarioState
    String currentUserLogin;

    @ExpectedScenarioState
    CurrentStep currentStep;

    public SELF retrive_a_new_id() {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        String baseUrl = getBaseUrl();
        Request request = new Request.Builder().post(emptyBody).url(baseUrl + "/api/v1/user").build();
        try {
            Response response = httpClient.newCall(request).execute();
            if (response.code() != 200) {
                fail("Invalid HTTP code status " + response.code() + " expected 200");
            }
            newUserId = response.body().string();
            response.body().close();
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
        return self();
    }

    public SELF create_user_with_email_$(@Quoted String email) {
        return create_user(email, true);
    }

    public SELF create_user_with_email_$_which_must_fail(@Quoted String email) {
        return create_user(email, false);
    }

    private SELF create_user(String email, boolean success) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("email must be defined.");
        }
        if (success && StringUtils.isBlank(newUserId)) {
            retrive_a_new_id();
        }
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), ("{\"email\": \"" + email + "\"}").getBytes());
        String baseUrl = getBaseUrl();

        Request.Builder builder = new Request.Builder().post(body).url(baseUrl + "/api/v1/user" + (newUserId != null ? "/" + newUserId : ""));
        if (isNotBlank(currentUserLogin)) {
            UserInfo currentUser = currentUsers.get(currentUserLogin);
            if (currentUser != null) {
                builder = StageUtils.addBasicAuthentification(currentUser, builder);
            }
        }
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            if (success) {
                assertThat(response.code()).isEqualTo(201);
                JsonParser parser = new JsonParser();
                String bodyResponse = response.body().string();
                JsonObject json = (JsonObject) parser.parse(bodyResponse);
                String currentUsername = json.getAsJsonPrimitive("username").getAsString();
                String currentUserPassword = json.getAsJsonPrimitive("password").getAsString();
                String currentUserEmail = json.getAsJsonPrimitive("email").getAsString();
                String currentUserIdentifier = json.getAsJsonPrimitive("identifier").getAsString();
                currentUsers.put(currentUsername, new UserInfo(currentUsername, currentUserIdentifier, currentUserPassword, currentUserEmail));
                if (isBlank(currentUserLogin)) {
                    currentUserLogin = currentUsername;
                }
                Attachment privateKey = Attachment.plainText(bodyResponse).withTitle(currentUsername + " response");
                currentStep.addAttachment(privateKey);
            } else {
                assertThat(response.code()).isNotEqualTo(201);
            }
            response.body().close();
        } catch (IOException e) {
            if (success) {
                fail(e.getMessage(), e);
            }
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return self();
    }

    public SELF create_a_new_project_configuration_with_name_$(String projectName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        //  Mock behavior
        BootstrapStackData boostrapData = new BootstrapStackData(projectName, "build-A", "127.0.0.1", 10022);
        Mockito.when(projectManager.bootstrapStack(projectName, "build-A", StackType.BUILD)).thenReturn(boostrapData);
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        Set<Stack> stacks = new HashSet<>();
        stacks.add(new Stack("build-A", StackType.BUILD, new HashSet<BrickState>()));
        Project project = new Project("1234567890", projectName, SSLUtils.createSelfSignedSSLKeyPair(projectName, (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic()), new Date(), stacks);
        try {
            Mockito.when(projectManager.start(Mockito.any())).thenReturn(project);
        } catch (ProjectAlreadyExistException e) {
            fail(e.getMessage());
        }

        //  Send project configuration configuration
        UserInfo currentUser = currentUsers.get(currentUserLogin);

        String json = "{\n" +
                "   \"name\": \"" + projectName + "\",\n" +
                "   \"ownerIdentifier\": \"" + currentUser.getIdentifier() + "\"\n" +
                "}";

        OkHttpClient httpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig").post(body);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            projectConfigurationId = response.body().string();
            String projectConfiguration = getProjectConfiguration(currentUserLogin, projectConfigurationId);
            currentStep.addAttachment(Attachment.plainText(projectConfiguration).withTitle("Project configuration for " + projectName).withFileName("projectconfiguration_" + projectName + ".json"));
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return self();
    }

    private String getProjectConfiguration(String username, String projectConfigurationId) {
        UserInfo userInfo = currentUsers.get(username);
        Request.Builder builder = new Request.Builder().get().url(getApiBaseUrl() + "/projectconfig/" + projectConfigurationId);
        Request request = StageUtils.addBasicAuthentification(userInfo, builder).build();
        Response response = null;
        try {
            OkHttpClient httpClient = new OkHttpClient();
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);
            String body = response.body().string();
            return body;
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return null;

    }

    public SELF add_user_$_to_project_configuration(@Quoted String username) {
        UserInfo currentUser = currentUsers.get(currentUserLogin);
        UserInfo userToAdd = currentUsers.get(username);

        String json = "[\n" +
                "    \"" + userToAdd.getIdentifier() + "\"\n" +
                "  ]";


        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig/" + projectConfigurationId + "/user").put(body);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();

        executeRequestWithExpectedStatus(request, 200);

        return self();
    }

    public SELF remove_user_$_to_project_configuration(String usernameToDelete) {
        UserInfo currentUser = currentUsers.get(currentUserLogin);
        UserInfo userToAdd = currentUsers.get(usernameToDelete);

        String json = "[\n" +
                "    \"" + userToAdd.getIdentifier() + "\"\n" +
                "  ]";

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig/" + projectConfigurationId + "/user").delete(body);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();

        executeRequestWithExpectedStatus(request, 200);

        return self();
    }

    private void executeRequestWithExpectedStatus(Request request, int expectedStatus) {
        OkHttpClient httpClient = new OkHttpClient();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(expectedStatus);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }

    private String getApiBaseUrl() {
        return getBaseUrl() + "/api/v1";
    }

}
