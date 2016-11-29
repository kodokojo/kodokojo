package io.kodokojo.commons.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Event<T extends Serializable> {

    private final String version = "1.0.0";

    private final Header headers;

    private final T payload;

    public Event(Header headers, T payload) {
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

    public long getCreationDate(){
        return headers.getCreationDate();
    }

    public String getFrom() {
        return headers.getFrom();
    }

    public String getEventType() {
        return headers.getEventType();
    }

    public Map<String, String> getCustom() {
        return headers.getCustom();
    }

    public T getPayload() {
        return payload;
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

    static class Header {

        private final Category category;

        private final String from;

        private final long creationDate;

        private final String eventType;

        private final Map<String, String> custom;

        public Header(Category category, String from, long creationDate, String eventType, Map<String, String> custom) {
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
            this.creationDate = creationDate;
            this.eventType = eventType;
        }

        public Header(Category category, String from, long creationDate, String eventType) {
            this(category, from, creationDate, eventType, null);
        }


        public Category getCategory() {
            return category;
        }

        public String getFrom() {
            return from;
        }

        public long getCreationDate() {
            return creationDate;
        }

        public String getEventType() {
            return eventType;
        }

        public Map<String, String> getCustom() {
            return new HashMap<>(custom);
        }
    }

    public static <T extends Serializable> Event<T> buildFromJson(Class<T> payloadType, String json) {
        requireNonNull(payloadType, "payloadType must be defined.");
        if (isBlank(json)) {
            throw new IllegalArgumentException("json must be defined.");
        }
        Gson gson = new GsonBuilder().create();
        Event event = gson.fromJson(json, Event.class);
        Serializable payload = event.getPayload();
        if (payload == null || ! payloadType.isAssignableFrom(payload.getClass())) {
            throw new IllegalArgumentException("payload type " + (payload == null ? "null" : payload.getClass().getCanonicalName()) + " is not assignable to required type " + payloadType.getCanonicalName() + ".");
        }
        return event;
    }

    public static String convertToJson(Event event) {
        requireNonNull(event, "event must be defined.");
        Gson gson = new GsonBuilder().create();
        return gson.toJson(event);
    }

}
