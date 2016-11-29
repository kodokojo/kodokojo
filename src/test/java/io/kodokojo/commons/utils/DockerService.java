package io.kodokojo.commons.utils;

import io.kodokojo.commons.model.PortDefinition;
import io.kodokojo.commons.model.Service;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerService extends Service {

    private final String containerId;

    public DockerService(String containerId, String name, String host, PortDefinition portDefinition) {
        super(name, host, portDefinition);
        if (isBlank(containerId)) {
            throw new IllegalArgumentException("containerId must be defined.");
        }
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
