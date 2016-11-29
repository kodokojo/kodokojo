package io.kodokojo.commons.model;

import java.io.Serializable;

public class UserInWaitingList implements Serializable {

    private final String username;

    private final String email;

    private final long waitingSince;

    public UserInWaitingList(String username, String email, long waitingSince) {
        this.username = username;
        this.email = email;
        this.waitingSince = waitingSince;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getWaitingSince() {
        return waitingSince;
    }

    @Override
    public String toString() {
        return "UserInWaitingList{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", waitingSince=" + waitingSince +
                '}';
    }
}
