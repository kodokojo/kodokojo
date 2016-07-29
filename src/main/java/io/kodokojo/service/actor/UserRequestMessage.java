package io.kodokojo.service.actor;

import io.kodokojo.model.User;

import java.io.Serializable;

public class UserRequestMessage implements Serializable {

    protected final User requester;

    public UserRequestMessage(User requester) {
        this.requester = requester;
    }

    public User getRequester() {
        return requester;
    }

    @Override
    public String toString() {
        return "UserRequestMessage{" +
                "requester=" + requester +
                '}';
    }
}
