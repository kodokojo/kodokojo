package io.kodokojo.project.gitlab;

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

import com.google.gson.*;
import com.squareup.okhttp.Response;
import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.project.model.UserService;
import io.kodokojo.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

import java.security.interfaces.RSAPrivateKey;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GitlabUserManager  {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUserManager.class);

    private final String privateToken;

    private final GitlabRest gitlabRest;

    public GitlabUserManager(String baseUrl, String privateToken) {
        if (isBlank(baseUrl)) {
            throw new IllegalArgumentException("baseUrl must be defined.");
        }
        if (isBlank(privateToken)) {
            throw new IllegalArgumentException("privateToken must be defined.");
        }
        this.privateToken = privateToken;

        Gson gson = new GsonBuilder().create();
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(baseUrl + "api/v3").setConverter(new GsonConverter(gson)).build();
        gitlabRest = restAdapter.create(GitlabRest.class);
    }

    public boolean createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must be defined.");
        }
        try {
            JsonObject jsonObject = gitlabRest.createUser(privateToken, user.getUsername(), user.getPassword(), user.getEmail(), user.getName(), "false");
            int id = jsonObject.getAsJsonPrimitive("id").getAsInt();

            Response response = gitlabRest.addSshKey(privateToken, Integer.toString(id), "SSH Key", user.getSshPublicKey());
            return response.code() == 201;

        } catch (RetrofitError e) {
            LOGGER.error("unable to complete creation of user : " + e.getBody(), e);
        }
        return false;
    }

}
