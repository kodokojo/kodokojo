/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class JsonUtils {

    private interface JsonGetter<T> {
        T extract(JsonPrimitive jsonPrimitive);
    }

    private static <T> Optional<T> readFromJson(JsonObject json, String name, JsonGetter<T> mapper) {
        return Optional.ofNullable(json.getAsJsonPrimitive(name)).flatMap(jsonPrimitive -> Optional.of(mapper.extract(jsonPrimitive)));

    }

    public static Optional<String> readStringFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsString);
    }

    public static Optional<Integer> readIntFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsInt);
    }

    public static Optional<Long> readLongFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsLong);
    }

    public static Optional<Boolean> readBooleanFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return readFromJson(json, name, JsonPrimitive::getAsBoolean);
    }

    public static Optional<JsonObject> readJsonObjectFromJson(JsonObject json, String name) {
        requireNonNull(json, "json must be defined.");
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return Optional.ofNullable(json.getAsJsonObject(name));
    }

}
