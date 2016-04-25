package io.kodokojo.entrypoint.dto;

import com.google.gson.JsonObject;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class WebSocketMessage implements Serializable {

    private final String entity;

    private final String action;

    private final JsonObject data;

    public WebSocketMessage(String entity, String action, JsonObject data) {
        if (isBlank(entity)) {
            throw new IllegalArgumentException("entity must be defined.");
        }
        if (isBlank(action)) {
            throw new IllegalArgumentException("action must be defined.");
        }
        if (data == null) {
            throw new IllegalArgumentException("data must be defined.");
        }
        this.entity = entity;
        this.action = action;
        this.data = data;
    }

    public String getEntity() {
        return entity;
    }

    public String getAction() {
        return action;
    }

    public JsonObject getData() {
        return data;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "entity='" + entity + '\'' +
                ", action='" + action + '\'' +
                ", data=" + data +
                '}';
    }
}
