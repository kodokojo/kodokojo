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
