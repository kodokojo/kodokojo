package io.kodokojo.bdd.stage;

import com.squareup.okhttp.Request;

import java.util.Base64;

public class StageUtils {

    private StageUtils() {
        //
    }

    public static Request.Builder addBasicAuthentification(UserInfo user, Request.Builder builder) {
        assert user != null : "user must be defined";
        assert builder != null : "builder must be defined";

        String value = "Basic " + Base64.getEncoder().encodeToString((user.getUsername() + ":" + user.getPassword()).getBytes());
        builder.addHeader("Authorization", value);
        return builder;
    }

}
