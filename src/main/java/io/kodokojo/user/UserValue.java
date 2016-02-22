package io.kodokojo.user;

import io.kodokojo.commons.project.model.User;

import java.io.Serializable;

public class UserValue implements Serializable {

    private String name;

    private String username;

    private String email;

    private byte[] password;

    private String sshPublicKey;

    public UserValue(String name, String username, String email, byte[] password, String sshPublicKey) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sshPublicKey = sshPublicKey;
    }

    public UserValue(User user, byte[] password) {
        this(user.getName(), user.getUsername(), user.getEmail(), password, user.getSshPublicKey());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setSshPublicKey(String sshPublicKey) {
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

    public byte[] getPassword() {
        return password;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    @Override
    public String toString() {
        return "UserValue{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", sshPublicKey='" + sshPublicKey + '\'' +
                '}';
    }
}
