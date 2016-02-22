package io.kodokojo.user;

import io.kodokojo.commons.project.model.UserService;

import java.io.Serializable;

public class UserServiceValue implements Serializable {

    private String name;

    private String login;

    private byte[] password;

    private byte[] privateKey;

    private byte[] publicKey;

    public UserServiceValue(String name, String login, byte[] password, byte[] privateKey, byte[] publicKey) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public UserServiceValue(UserService userService, byte[] password, byte[] privateKey, byte[] publicKey) {
        this(userService.getName(), userService.getLogin(), password, privateKey, publicKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getLogin() {
        return login;
    }

    public byte[] getPassword() {
        return password;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "UserServiceValue{" +
                "name='" + name + '\'' +
                ", login='" + login + '\'' +
                '}';
    }
}
