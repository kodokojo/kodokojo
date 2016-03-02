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
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.model.User;
import io.kodokojo.user.redis.RedisUserManager;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationThen<SELF extends ApplicationThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    RedisUserManager userManager;

    @ExpectedScenarioState
    CurrentStep currentStep;

    @ExpectedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

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

}
