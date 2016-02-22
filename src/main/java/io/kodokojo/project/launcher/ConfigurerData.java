package io.kodokojo.project.launcher;

import io.kodokojo.commons.project.model.User;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ConfigurerData {

    private final String entrypoint;

    private final List<User> users;

    public ConfigurerData(String entrypoint, List<User> users) {
        if (isBlank(entrypoint)) {
            throw new IllegalArgumentException("entrypoint must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        this.entrypoint = entrypoint;
        this.users = users;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public List<User> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "ConfigurerData{" +
                "entrypoint='" + entrypoint + '\'' +
                ", users=" + users +
                '}';
    }
}
