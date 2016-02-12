package io.kodokojo.project.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Service {

    private final Brick brick;

    private final String connectionUrl;

    public Service(Brick brick, String connectionUrl) {
        if (brick == null) {
            throw new IllegalArgumentException("brick must be defined.");
        }
        if (isBlank(connectionUrl)) {
            throw new IllegalArgumentException("connectionUrl must be defined.");
        }
        this.brick = brick;
        this.connectionUrl = connectionUrl;
    }

    public Brick getBrick() {
        return brick;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String toString() {
        return "Service{" +
                "brick=" + brick +
                ", connectionUrl='" + connectionUrl + '\'' +
                '}';
    }
}
