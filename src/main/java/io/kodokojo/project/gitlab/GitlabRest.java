package io.kodokojo.project.gitlab;

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
