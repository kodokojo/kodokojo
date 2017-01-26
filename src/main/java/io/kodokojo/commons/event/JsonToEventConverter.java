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
        GsonEventSerializer gsonEventSerializer = new GsonEventSerializer();
        JsonParser parser = new JsonParser();
        return gsonEventSerializer.deserialize(parser.parse(input), null, null);
/*
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
                if (headers.has("replyTo")) {
                    builder.setReplyTo(headers.getAsJsonPrimitive("replyTo").getAsString());
                }
                if (headers.has("correlationId")) {
                    builder.setCorrelationId(headers.getAsJsonPrimitive("correlationId").getAsString());
                }
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
            if (json.has("payload")) {
                builder.setJsonPayload(json.get("payload").toString());
            } else {
                builder.setJsonPayload("");
            }
            return builder.build();
        }
        return null;
    */
    }

}
