package io.kodokojo.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import io.kodokojo.config.ReCaptchaConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ReCaptchaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReCaptchaService.class);

    private static String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final ReCaptchaConfig reCaptchaConfig;

    private final OkHttpClient httpClient;

    @Inject
    public ReCaptchaService(ReCaptchaConfig reCaptchaConfig, OkHttpClient httpClient) {
        if (reCaptchaConfig == null) {
            throw new IllegalArgumentException("reCaptchaConfig must be defined.");
        }
        if (httpClient == null) {
            throw new IllegalArgumentException("httpClient must be defined.");
        }
        this.httpClient = httpClient;
        this.reCaptchaConfig = reCaptchaConfig;
    }

    public boolean isConfigured() {
        return StringUtils.isNotBlank(reCaptchaConfig.secret());
    }

    public boolean validToken(String token, String userIp) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("token must be defined.");
        }
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder().add("secret", reCaptchaConfig.secret())
                .add("response", token);
        if (StringUtils.isNotBlank(userIp)) {
            formEncodingBuilder.add("remoteip", userIp);
        }
        Request request = new Request.Builder().post(formEncodingBuilder.build()).url(RECAPTCHA_URL).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            if (response.code() == 200) {
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(response.body().charStream());
                return json.getAsJsonPrimitive("success").getAsBoolean();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to request ReCaptcha to validate the user Token", e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }
}
