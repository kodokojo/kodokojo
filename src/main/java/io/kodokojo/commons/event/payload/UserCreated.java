package io.kodokojo.commons.event.payload;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserCreated implements Serializable {

    private final String identifier;

    private final String username;

    private final String email;

    public UserCreated(String identifier, String username, String email) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        if (isBlank(email)) {
            throw new IllegalArgumentException("email must be defined.");
        }
        this.identifier = identifier;
        this.username = username;
        this.email = email;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UserCreated{" +
                "identifier='" + identifier + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
