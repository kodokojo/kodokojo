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
package io.kodokojo.commons.endpoint.dto;

import com.google.gson.*;

import java.lang.reflect.Type;

public class WebSocketMessageGsonAdapter implements JsonDeserializer<WebSocketMessage>, JsonSerializer<WebSocketMessage> {

    @Override
    public WebSocketMessage deserialize(JsonElement input, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = (JsonObject) input;
        JsonPrimitive jsonEntity = json.getAsJsonPrimitive("entity");
        if (jsonEntity == null) {
            throw new JsonParseException("entity attribute expected.");
        }
        String entity = jsonEntity.getAsString();
        JsonPrimitive jsonAction = json.getAsJsonPrimitive("action");
        if (jsonAction == null) {
            throw new JsonParseException("action attribute expected.");
        }
        String action = jsonAction.getAsString();
        JsonObject jsonData = json.getAsJsonObject("data");
        if (jsonData == null) {
            throw new JsonParseException("data attribute expected.");
        }

        return new WebSocketMessage(entity, action, jsonData);
    }

    @Override
    public JsonElement serialize(WebSocketMessage src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject res = new JsonObject();
        res.addProperty("entity", src.getEntity());
        res.addProperty("action", src.getAction());
        res.add("data", src.getData());
        return res;
    }
}
