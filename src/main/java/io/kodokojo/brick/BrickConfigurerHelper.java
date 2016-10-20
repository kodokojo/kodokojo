package io.kodokojo.brick;


import okhttp3.Request;

import java.util.Base64;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public interface BrickConfigurerHelper {

    default void addBasicAuthentificationHeader(Request.Builder builder, String login, String password) {
        requireNonNull(builder, "builder must be defined.");
        if (isBlank(login)) {
            throw new IllegalArgumentException("login must be defined.");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("password must be defined.");
        }
        String encodedCredentials = Base64.getEncoder().encodeToString(String.format("%s:%s", login, password).getBytes());
        builder.addHeader("Authorization", "Basic " + encodedCredentials);
    }

}
