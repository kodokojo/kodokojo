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

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GsonEventSerializer implements JsonSerializer<Event>, JsonDeserializer<Event> {
    @Override
    public Event deserialize(JsonElement el, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject json = (JsonObject) el;
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
                if (headers.has("redeliveryCount")) {
                    Integer redeliveryCount = headers.getAsJsonPrimitive("redeliveryCount").getAsNumber().intValue();
                    builder.setRedeliveryCount(redeliveryCount);
                }
                if (headers.has("requestReplyType")) {
                    String requestReplyTypeAsString = headers.getAsJsonPrimitive("requestReplyType").getAsString();
                    builder.setRequestReplyType(Event.RequestReplyType.valueOf(requestReplyTypeAsString));
                }
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

            JsonElement payload = json.get("payload");
            if (payload == null) {
                builder.setJsonPayload("");
            } else {
                if (payload.isJsonObject()) {
                    builder.setJsonPayload(payload.toString());
                } else {
                    builder.setJsonPayload(payload.getAsString());
                }
            }
            return builder.build();
        }
        return null;
    }

    @Override
    public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
        requireNonNull(src, "src must be defined.");
        JsonObject root = new JsonObject();
        root.addProperty("version", src.getVersion());
        JsonObject headers = new JsonObject();
        root.add("headers", headers);
        headers.addProperty("category", src.getCategory().name());
        headers.addProperty("from", src.getFrom());
        headers.addProperty("requestReplyType", src.getRequestReplyType().name());
        headers.addProperty("replyTo", src.getReplyTo());
        headers.addProperty("creationDate", src.getCreationDate());
        headers.addProperty("correlationId", src.getCorrelationId());
        headers.addProperty("eventType", src.getEventType());
        headers.addProperty("redeliveryCount", src.getRedeliveryCount());
        JsonObject custom = new JsonObject();
        headers.add("custom", custom);
        for(Map.Entry<String, String> entry : src.getCustom().entrySet()) {
            custom.addProperty(entry.getKey(), entry.getValue());
        }
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(src.getPayload());
        root.add("payload", parse);

        return root;
    }
}
