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
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class EventBuilder {

    private Event.Category category;

    private String from;

    private Event.RequestReplyType requestReplyType;

    private String replyTo;

    private String correlationId;

    private long creationDate;

    private String eventType;

    private Map<String, String> custom;

    private String payload;

    private long ttl;

    private int redeliveryCount = 0;

    private int maxRedeliveryCount;

    public EventBuilder() {
        super();
    }

    public EventBuilder(Event copyFrom) {
        super();
        requireNonNull(copyFrom, "copyFrom must be defined.");
        category = copyFrom.getCategory();
        from = copyFrom.getFrom();
        requestReplyType = copyFrom.getRequestReplyType();
        replyTo = copyFrom.getReplyTo();
        correlationId = copyFrom.getCorrelationId();
        creationDate = copyFrom.getCreationDate();
        ttl = copyFrom.getTtl();
        maxRedeliveryCount = copyFrom.getMaxRedeliveryCount();
        redeliveryCount = copyFrom.getRedeliveryCount();
        eventType = copyFrom.getEventType();
        custom = copyFrom.getCustom() == null ? new HashMap<>() : new HashMap<>(copyFrom.getCustom());
        payload = copyFrom.getPayload();
    }

    public Event build() {
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
        if (requestReplyType == null) {
            requestReplyType = Event.RequestReplyType.NONE;
        }
        return new Event(new Event.Header(category, from, requestReplyType, replyTo, correlationId, creationDate, ttl, redeliveryCount, maxRedeliveryCount, eventType, custom), payload);
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

    public EventBuilder setRequestReplyType(Event.RequestReplyType requestReplyType) {
        this.requestReplyType = requestReplyType;
        return this;
    }

    public EventBuilder setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public EventBuilder setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public EventBuilder setCreationDate(long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public EventBuilder setTtl(long ttl) {
        this.ttl = ttl;
        return this;
    }

    public EventBuilder setRedeliveryCount(int redeliveryCount) {
        this.redeliveryCount = redeliveryCount;
        return this;
    }

    public EventBuilder incrementRedeliveryCount() {
        this.redeliveryCount++;
        return this;
    }

    public EventBuilder setMaxRedeliveryCount(int maxRedeliveryCount) {
        this.maxRedeliveryCount = maxRedeliveryCount;
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

    public EventBuilder copyCustomHeader(Event from, String header) {
        requireNonNull(from, "from must be defined.");
        if (isBlank(header)) {
            throw new IllegalArgumentException("header must be defined.");
        }
        Map<String, String> fromCustom = from.getCustom();
        if (MapUtils.isNotEmpty(fromCustom) && fromCustom.containsKey(header)) {
            custom.put(header, fromCustom.get(header));
        }
        return this;
    }

    public EventBuilder setJsonPayload(String payload) {
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

    public EventBuilder setEvent(Event copyFrom) {
        category = copyFrom.getCategory();
        requestReplyType = copyFrom.getRequestReplyType();
        replyTo = copyFrom.getReplyTo();
        correlationId = copyFrom.getCorrelationId();
        creationDate = copyFrom.getCreationDate();
        eventType = copyFrom.getEventType();
        custom = copyFrom.getCustom();
        payload = copyFrom.getPayload();
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

    public EventBuilder addCustomHeader(String key, String value) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (custom == null) {
            custom = new HashMap<>();
        }
        custom.put(key, value);
        return this;
    }
}
