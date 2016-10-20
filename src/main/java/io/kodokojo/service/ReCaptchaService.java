/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.config.ReCaptchaConfig;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
        FormBody.Builder formEncodingBuilder = new FormBody.Builder().add("secret", reCaptchaConfig.secret())
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
                boolean success = json.getAsJsonPrimitive("success").getAsBoolean();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("User with IP {} successfully validate her Captcha.", userIp);
                }
                return success;
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
