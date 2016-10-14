package io.kodokojo.utils;

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
