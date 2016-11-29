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
package io.kodokojo.commons.endpoint;

import com.google.gson.*;
import io.kodokojo.commons.model.User;
import io.kodokojo.service.authentification.SimpleCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.ResponseTransformer;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public abstract class AbstractSparkEndpoint implements SparkEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSparkEndpoint.class);

    private static final String API_VERSION = "v1";

    public static final String BASE_API = "/api/" + API_VERSION;

    protected static final String JSON_CONTENT_TYPE = "application/json";

    protected final ThreadLocal<Gson> localGson = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new GsonBuilder().create();
        }
    };

    protected final ResponseTransformer jsonResponseTransformer = new JsonTransformer();

    protected final UserAuthenticator<SimpleCredential> userAuthenticator;

    protected AbstractSparkEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator) {
        requireNonNull(userAuthenticator, "userAuthenticator must be defined.");
        this.userAuthenticator = userAuthenticator;
    }

    protected SimpleCredential extractCredential(Request request) {
        BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
        try {
            basicAuthenticator.handle(request, null);
            if (basicAuthenticator.isProvideCredentials()) {
                return new SimpleCredential(basicAuthenticator.getUsername(), basicAuthenticator.getPassword());
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve credentials", e);
        }
        return null;
    }

    protected User getRequester(Request request) {
        SimpleCredential credential = extractCredential(request);
        if (credential != null) {
            return userAuthenticator.authenticate(credential);
        }
        return null;
    }

    private interface JsonGetter<T> {
        T extract(JsonPrimitive jsonPrimitive);
    }

    private static <T> Optional<T> readFromJson(JsonObject json, String name, JsonGetter<T> mapper) {
        return Optional.ofNullable(json.getAsJsonPrimitive(name)).flatMap(jsonPrimitive -> Optional.of(mapper.extract(jsonPrimitive)));

    }

    protected static Optional<String> readStringFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsString);
    }

    protected static Optional<Integer> readIntFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsInt);
    }

    protected static Optional<Long> readLongFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsLong);
    }

    protected static Optional<Boolean> readBooleanFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsBoolean);
    }



}
