package io.kodokojo.entrypoint.dto;

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
