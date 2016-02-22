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

import com.google.gson.JsonObject;
import com.squareup.okhttp.Response;
import retrofit.http.*;

public interface GitlabRest {

    @FormUrlEncoded
    @POST("/users")
    JsonObject createUser(@Header("PRIVATE-TOKEN") String privateToken, @Field("username") String username, @Field("password") String password, @Field("email") String email, @Field("name") String name, @Field("confirm") String confirmationExpected);

    @FormUrlEncoded
    @POST("/users/{id}/keys")
    Response addSshKey(@Header("PRIVATE-TOKEN") String privateToken, @Path("id") String id, @Field("title") String title, @Field("key") String key);

}
