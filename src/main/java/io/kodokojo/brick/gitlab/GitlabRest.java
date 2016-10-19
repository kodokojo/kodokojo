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
package io.kodokojo.brick.gitlab;



import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import retrofit2.Response;
import retrofit2.http.*;

public interface GitlabRest {

    String PRIVATE_TOKEN = "PRIVATE-TOKEN";

    String API_V3_USERS = "/api/v3/users/";

    @FormUrlEncoded
    @POST(API_V3_USERS)
    JsonObject createUser(@Header(PRIVATE_TOKEN) String privateToken, @Field("username") String username, @Field("password") String password, @Field("email") String email, @Field("name") String name, @Field("confirm") String confirmationExpected);

    @FormUrlEncoded
    @POST(API_V3_USERS + "{id}/keys")
    Response addSshKey(@Header(PRIVATE_TOKEN) String privateToken, @Path("id") String id, @Field("title") String title, @Field("key") String key);

    @GET(API_V3_USERS + "{id}/keys")
    JsonArray listSshKeys(@Header(PRIVATE_TOKEN) String privateToken, @Path("id") String id);

    @DELETE(API_V3_USERS + "{id}/keys/{keyId}")
    Response deleteSshKey(@Header(PRIVATE_TOKEN) String privateToken, @Path("id") String id, @Path("keyId") String keyId);

    @GET(API_V3_USERS)
    JsonArray searchByUsername(@Header(PRIVATE_TOKEN) String privateToken, @Query("username") String username);

    @FormUrlEncoded
    @PUT(API_V3_USERS + "{id}")
    Response update(@Header(PRIVATE_TOKEN) String privateToken,
                    @Path("id") String id,
                    @Field("username") String username,
                    @Field("name") String name,
                    @Field("password") String password,
                    @Field("email") String email);


    @DELETE(API_V3_USERS + "{id}")
    Response deleteUser(@Header(PRIVATE_TOKEN) String privateToken, @Path("id") String id);

}
