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
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.entrypoint.RestEntrypoint;
import org.assertj.core.api.Assertions;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ApplicationWhen<SELF extends ApplicationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    RestEntrypoint restEntrypoint;

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    @ProvidedScenarioState
    String newUserId;

    public SELF retrive_a_new_id() {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        String baseUrl = "http://" + restEntryPointHost + ":" + restEntryPointPort;
        Request request = new Request.Builder().post(emptyBody).url(baseUrl + "/api/user").build();
        try {
            Response response = httpClient.newCall(request).execute();
            if (response.code() != 200) {
                fail("Invalid HTTP code status " + response.code() + " expected 200");
            }
            newUserId = response.body().string();
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
        return self();
    }

    public SELF create_user_with_email_$(@Quoted String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("email must be defined.");
        }
        if (isBlank(newUserId)) {
            throw new IllegalArgumentException("newUserId must be defined.");
        }
        OkHttpClient httpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), ("{\"email\": \"" + email +"\"}").getBytes());
        String baseUrl = "http://" + restEntryPointHost + ":" + restEntryPointPort;

        Request request = new Request.Builder().put(body).url(baseUrl + "/api/user/" + newUserId).build();
        try {
            Response response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            System.out.println(response.body().string());
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
        return self();
    }
}
