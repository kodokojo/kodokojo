package io.kodokojo.commons.event.payload;

import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.model.User;
import io.kodokojo.commons.service.actor.message.EventBusOriginMessage;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class OrganisationChangeUserRequest implements EventBusOriginMessage, Serializable {

    public enum TypeChange {
        ADD,
        REMOVE
    }

    private final User requester;

    private final String organisationId;

    private final String userId;

    private final TypeChange typeChange;

    private Event request;

    public OrganisationChangeUserRequest(User requester,TypeChange typeChange,  String organisationId, String userId) {
        requireNonNull(requester, "requester must be defined.");
        requireNonNull(typeChange, "typeChange must be defined.");
        if (isBlank(organisationId)) {
            throw new IllegalArgumentException("organisationId must be defined.");
        }
        if (isBlank(userId)) {
            throw new IllegalArgumentException("userId must be defined.");
        }
        this.requester = requester;
        this.typeChange = typeChange;
        this.organisationId = organisationId;
        this.userId = userId;
    }

    public void setRequest(Event request) {
        this.request = request;
    }

    @Override
    public Event originalEvent() {
        return null;
    }

    public User getRequester() {
        return requester;
    }

    public String getUserId() {
        return userId;
    }

    public TypeChange getTypeChange() {
        return typeChange;
    }

    public String getOrganisationId() {
        return organisationId;
    }
}
