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
import io.kodokojo.model.Stack;
import io.kodokojo.service.ProjectAlreadyExistException;
import io.kodokojo.service.ProjectManager;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;
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

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), ("{\"email\": \"" + email + "\"}").getBytes());
        String baseUrl = getBaseUrl();

        Request request = new Request.Builder().post(body).url(baseUrl + "/api/v1/user" + (newUserId != null ? "/" + newUserId : "")).build();
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

        BootstrapStackData boostrapData = new BootstrapStackData(projectName, "build-A", "127.0.0.1", 10022);
        Mockito.when(projectManager.bootstrapStack(projectName, "build-A", StackType.BUILD)).thenReturn(boostrapData);
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        Set<Stack> stacks = new HashSet<>();
        stacks.add(new Stack("build-A", StackType.BUILD, Stack.OrchestratorType.MARATHON, new HashSet<BrickDeploymentState>()));
        Project project = new Project("1234567890", projectName, SSLUtils.createSelfSignedSSLKeyPair(projectName, (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic()), new Date(), stacks);
        try {
            Mockito.when(projectManager.start(Mockito.any())).thenReturn(project);
        } catch (ProjectAlreadyExistException e) {
            fail(e.getMessage());
        }

        UserInfo currentUser = currentUsers.get(currentUserLogin);

        String json = "{\n" +
                "   \"name\": \"" + projectName + "\",\n" +
                "   \"ownerIdentifier\": \"" + currentUser.getIdentifier() + "\"\n" +
                "}";

        System.out.println(json);

        OkHttpClient httpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig").post(body);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            projectConfigurationId = response.body().string();
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return self();
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }

    private String getApiBaseUrl() {
        return getBaseUrl() + "/api/v1";
    }



}
