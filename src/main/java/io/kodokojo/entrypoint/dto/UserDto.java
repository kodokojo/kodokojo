package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.User;

import java.io.Serializable;

public class UserDto implements Serializable {

    private String identifer;

    private String username;

    public UserDto(String identifer, String username) {
        this.identifer = identifer;
        this.username = username;
    }

    public UserDto(User owner) {

    }

    public String getIdentifer() {
        return identifer;
    }

    public void setIdentifer(String identifer) {
        this.identifer = identifer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "identifer='" + identifer + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
