package io.kodokojo.project.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class User {

    private final String name;

    private final String username;

    private final String email;

    private final String password;

    private final String sshPublicKey;

    public User(String name, String username, String email, String password, String sshPublicKey) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sshPublicKey = sshPublicKey;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + (password != null ? "DEFINED" : "NOT DEFINED") + '\'' +
                ", sshPublicKey='" +  (sshPublicKey != null ? "DEFINED" : "NOT DEFINED") + '\'' +
                '}';
    }
}
