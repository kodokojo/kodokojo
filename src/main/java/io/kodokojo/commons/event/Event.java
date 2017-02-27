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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Event implements Serializable {

    private static final String VERSION = "1.0.0";

    private final String version = VERSION;

    private final Header headers;

    private final String payload;

    public Event(Header headers, String payload) {
        requireNonNull(headers, "headers must be defined.");
        requireNonNull(payload, "payload must be defined.");
        this.headers = headers;
        this.payload = payload;
    }

    public String getVersion() {
        return version;
    }

    public Header getHeaders() {
        return headers;
    }

    public Category getCategory() {
        return headers.getCategory();
    }

    public long getCreationDate() {
        return headers.getCreationDate();
    }

    public String getFrom() {
        return headers.getFrom();
    }

    public RequestReplyType getRequestReplyType() {
        return headers.getRequestReplyType();
    }

    public String getReplyTo() {
        return headers.getReplyTo();
    }

    public String getCorrelationId() {
        return headers.getCorrelationId();
    }

    public String getEventType() {
        return headers.getEventType();
    }

    public long getTtl() {
        return headers.getTtl();
    }

    public int getRedeliveryCount() {
        return headers.getRedeliveryCount();
    }

    public int getMaxRedeliveryCount() {
        return headers.getMaxRedeliveryCount();
    }

    public Map<String, String> getCustom() {
        return headers.getCustom();
    }

    public String getPayload() {
        return payload;
    }

    public <T> T getPayload(Class<T> payloadType) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(payload, payloadType);
    }

    public String getPayloadAsJsonString() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(payload);
    }

    public enum Category {
        BUSINESS,
        TECHNICAL
    }

    public enum RequestReplyType {
        REQUEST,
        REPLY,
        NONE
    }

    static class Header implements Serializable {

        private final Category category;

        private final String from;

        private final String replyTo;

        private final String correlationId;

        private final RequestReplyType requestReplyType;

        private final long creationDate;

        private final String eventType;

        private final long ttl;

        private final int redeliveryCount;

        private final int maxRedeliveryCount;

        private final Map<String, String> custom;


        public Header(Category category, String from,  RequestReplyType requestReplyType, String replyTo, String correlationId, long creationDate, long ttl,int redeliveryCount, int maxRedeliveryCount, String eventType, Map<String, String> custom) {
            requireNonNull(category, "category must be defined.");
            if (isBlank(from)) {
                throw new IllegalArgumentException("from must be defined.");
            }
            if (isBlank(eventType)) {
                throw new IllegalArgumentException("eventType must be defined.");
            }
            if (custom == null) {
                this.custom = new HashMap<>();
            } else {
                this.custom = custom;
            }
            this.category = category;
            this.from = from;
            if (requestReplyType == null) {
                this.requestReplyType = RequestReplyType.NONE;
            } else {
                this.requestReplyType = requestReplyType;
            }
            this.replyTo = replyTo;
            this.correlationId = correlationId;
            this.creationDate = creationDate;
            this.eventType = eventType;
            this.ttl = ttl;
            this.redeliveryCount = redeliveryCount;
            this.maxRedeliveryCount = maxRedeliveryCount;
            if (requestReplyType == RequestReplyType.REQUEST) {
                if (isBlank(replyTo)) {
                    throw new IllegalArgumentException("Unable to create a Request without ReplyTo attribute");
                }
            }
        }


        public Header(Category category, String from,  RequestReplyType requestReplyType, String replyTo, String correlationId, long creationDate,String eventType, Map<String, String> custom) {
            this(category, from,requestReplyType, replyTo, correlationId,creationDate,0, -1, -1, eventType, custom);
        }

        public Header(Category category, String from, long creationDate, String eventType) {
            this(category, from,null, null, null, creationDate, eventType, null);
        }


        public Category getCategory() {
            return category;
        }

        public String getFrom() {
            return from;
        }

        public RequestReplyType getRequestReplyType() {
            return requestReplyType;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public long getCreationDate() {
            return creationDate;
        }

        public String getEventType() {
            return eventType;
        }

        public long getTtl() {
            return ttl;
        }

        public int getRedeliveryCount() {
            return redeliveryCount;
        }

        public int getMaxRedeliveryCount() {
            return maxRedeliveryCount;
        }

        public Map<String, String> getCustom() {
            return new HashMap<>(custom);
        }

        @Override
        public String toString() {
            return "Header{" +
                    "category=" + category +
                    ", from='" + from + '\'' +
                    ", replyTo='" + replyTo + '\'' +
                    ", requestReplyType='" + requestReplyType + '\'' +
                    ", correlationId='" + correlationId + '\'' +
                    ", creationDate=" + creationDate +
                    ", eventType='" + eventType + '\'' +
                    ", ttl=" + ttl +
                    ", redeliveryCount=" + redeliveryCount +
                    ", maxRedeliveryCount=" + maxRedeliveryCount +
                    ", custom=" + custom +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "version='" + version + '\'' +
                ", headers=" + headers +
                ", payload='" + payload + '\'' +
                '}';
    }

    public static Event buildFromJson(String json) {
        if (isBlank(json)) {
            throw new IllegalArgumentException("json must be defined.");
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).create();
        Event event = gson.fromJson(json, Event.class);
        return event;
    }

    public static String convertToJson(Event event) {
        requireNonNull(event, "event must be defined.");
        Gson gson = GSON_BUILDER.get().create();
        return gson.toJson(event);
    }

    public static String convertToPrettyJson(Event event) {
        requireNonNull(event, "event must be defined.");
        Gson gson = GSON_BUILDER.get().setPrettyPrinting().create();
        return gson.toJson(event);
    }

    private static final ThreadLocal<GsonBuilder> GSON_BUILDER = new ThreadLocal<GsonBuilder>() {
        @Override
        protected GsonBuilder initialValue() {
            return new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer());
        }
    };


    //  Technical
    public static final String SERVICE_CONNECT_TYPE = "service_connection";


    //  Business
    public static final String REQUESTER_ID_CUSTOM_HEADER = "requester_id";
    public static final String ENTITY_ID_CUSTOM_HEADER = "entity_id";
    public static final String PROJECTCONFIGURATION_ID_CUSTOM_HEADER = "projectconfiguration_id";
    public static final String BROADCAST_FROM_CUSTOM_HEADER = "broadcast_from";

    public static final String USER_CREATION_REQUEST = "user_creation_request";
    public static final String USER_CREATION_REPLY = "user_creation_reply";
    public static final String USER_CREATION_EVENT = "user_created";
    public static final String USER_IDENTIFIER_CREATION_REQUEST = "user_id_creation_request";
    public static final String USER_IDENTIFIER_CREATION_REPLY = "user_id_creation_reply";
    public static final String USER_UPDATE_REQUEST = "user_update_request";
    public static final String USER_UPDATE_REPLY = "user_update_reply";
    public static final String PROJECTCONFIG_CREATION_REQUEST = "projectconfig_creation_request";
    public static final String PROJECTCONFIG_CREATION_REPLY = "projectconfig_creation_reply";
    public static final String PROJECTCONFIG_CREATION_EVENT = "projectconfig_created";
    public static final String PROJECTCONFIG_CHANGE_USER_REQUEST = "projectconfig_change_user_request";
    public static final String PROJECTCONFIG_CHANGE_USER_REPLY = "projectconfig_change_user_reply";
    public static final String PROJECTCONFIG_START_REQUEST = "projectconfig_start_request";
    public static final String PROJECTCONFIG_START_REPLY = "projectconfig_start_reply";
    public static final String PROJECTCONFIG_STARTED = "projectconfig_started";
    public static final String PROJECT_CREATION_REQUEST = "project_creation_request";
    public static final String PROJECT_CREATION_REPLY = "project_creation_reply";
    public static final String USER_ADD_TO_PROJECT = "user_add_projectconfig";
    public static final String USER_REMOVE_TO_PROJECT = "user_remove_projectconfig";
    public static final String STACK_STARTED = "stack_started";
    public static final String BRICK_STATE_UPDATE = "brick_state_update";
    public static final String BRICK_PROPERTY_UPDATE_REQUEST = "brick_property_update_request";
    public static final String BRICK_PROPERTY_UPDATE_REPLY = "brick_property_update_reply";
    public static final String BRICK_STARTING = "starting";
    public static final String BRICK_CONFIGURING = "configuring";
    public static final String BRICK_RUNNING = "running";
    public static final String BRICK_ONFAILURE = "onfailure";
    public static final String BRICK_STOPPED = "stopped";
    public static final String ERROR_THROW_FROM = "error_throw_from";

}
