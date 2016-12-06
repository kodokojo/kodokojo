package io.kodokojo.commons.event.payload;

import java.io.Serializable;

public class UserCreationRequest implements Serializable {

    private final String id;

    private final String email;

    private final String username;

    private final String entityId;

    public UserCreationRequest(String id, String email, String username, String entityId) {

        this.id = id;
        this.email = email;
        this.username = username;
        this.entityId = entityId;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getEntityId() {
        return entityId;
    }
}
