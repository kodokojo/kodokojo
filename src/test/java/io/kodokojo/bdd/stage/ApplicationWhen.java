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
import io.kodokojo.entrypoint.RestEntrypoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    Map<String, UserInfo> currentUsers = new HashMap<>();

    @ExpectedScenarioState
    CurrentStep currentStep;


    public SELF retrive_a_new_id() {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        String baseUrl = betBaseUrl();
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
        String baseUrl = betBaseUrl();

        Request request = new Request.Builder().put(body).url(baseUrl + "/api/v1/user/" + (newUserId != null ? newUserId : "")).build();
        try {
            Response response = httpClient.newCall(request).execute();
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
        }
        return self();
    }

    private String betBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }
}
