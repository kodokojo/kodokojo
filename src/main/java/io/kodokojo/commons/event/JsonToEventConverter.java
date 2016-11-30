package io.kodokojo.commons.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public interface JsonToEventConverter {

    default Event converter(String input) {
        if (isBlank(input)) {
            throw new IllegalArgumentException("input must be defined.");
        }

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(input);
        if (json.has("version") && "1.0.0".equals(json.getAsJsonPrimitive("version").getAsString())) {
            EventBuilder builder = new EventBuilder();
            if (json.has("headers")) {
                JsonObject headers = json.getAsJsonObject("headers");
                Event.Category category = Event.Category.valueOf(headers.getAsJsonPrimitive("category").getAsString());
                builder.setCategory(category);
                String from = headers.getAsJsonPrimitive("from").getAsString();
                builder.setFrom(from);
                long creationDate = headers.getAsJsonPrimitive("creationDate").getAsLong();
                builder.setCreationDate(creationDate);
                String eventType = headers.getAsJsonPrimitive("eventType").getAsString();
                builder.setEventType(eventType);
                Map<String, String> customMap = new HashMap<>();
                if (headers.has("custom")) {
                    JsonObject custom = headers.getAsJsonObject("custom");
                    Set<Map.Entry<String, JsonElement>> entries = custom.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        customMap.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                builder.setCustom(customMap);
            }
            builder.setPayload(json.getAsJsonPrimitive("payload").getAsString());
            return builder.build();
        }
        return null;
    }

}
