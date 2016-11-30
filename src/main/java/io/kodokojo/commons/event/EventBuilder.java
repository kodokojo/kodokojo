package io.kodokojo.commons.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class EventBuilder {

    private Event.Category category;

    private String from;

    private long creationDate;

    private String eventType;

    private Map<String, String> custom;

    private String payload;

    public EventBuilder() {
        super();
    }

    public EventBuilder(Event copyFrom) {
        super();
        requireNonNull(copyFrom, "copyFrom must be defined.");
        category = copyFrom.getCategory();
        from = copyFrom.getFrom();
        creationDate = copyFrom.getCreationDate();
        eventType = copyFrom.getEventType();
        custom = copyFrom.getCustom();
        payload = copyFrom.getPayload();
    }

    public Event build() {
        requireNonNull(category, "category must be defined.");
        if (isBlank(from)) {
            throw new IllegalArgumentException("from must be defined.");
        }
        if (creationDate <= 0) {
            creationDate = System.currentTimeMillis();
        }
        if (isBlank(eventType)) {
            throw new IllegalArgumentException("eventType must be defined.");
        }
        if (custom == null) {
            custom = new HashMap<>();
        }
        if (category == null) {
            category = Event.Category.BUSINESS;
        }
        return new Event(new Event.Header(category, from, creationDate, eventType, custom), payload);
    }

    public EventBuilder setCategory(Event.Category category) {
        requireNonNull(category, "category must be defined.");
        this.category = category;
        return this;
    }

    public EventBuilder setFrom(String from) {
        if (isBlank(from)) {
            throw new IllegalArgumentException("from must be defined.");
        }
        this.from = from;
        return this;
    }

    public EventBuilder setCreationDate(long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public EventBuilder setEventType(String eventType) {
        if (isBlank(eventType)) {
            throw new IllegalArgumentException("eventType must be defined.");
        }
        this.eventType = eventType;
        return this;
    }

    public EventBuilder setCustom(Map<String, String> custom) {
        requireNonNull(custom, "custom must be defined.");
        this.custom = custom;
        return this;
    }

    public EventBuilder setPayload(String payload) {
        requireNonNull(payload, "payload must be defined.");
        this.payload = payload;
        return this;
    }

    public EventBuilder setPayload(Serializable payload) {
        requireNonNull(payload, "payload must be defined.");
        Gson gson = new GsonBuilder().create();
        this.payload = gson.toJson(payload);
        return this;
    }

    public Event.Category getCategory() {
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
        return custom;
    }

    public String getPayload() {
        return payload;
    }
}
