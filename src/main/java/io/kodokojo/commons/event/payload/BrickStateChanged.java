package io.kodokojo.commons.event.payload;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickStateChanged implements Serializable {

    private final String projectConfigurationId;

    private final String stackName;

    private final String brickName;

    private final String oldState;

    public BrickStateChanged(String projectConfigurationId, String stackName, String brickName, String oldState) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        if (isBlank(brickName)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        this.projectConfigurationId = projectConfigurationId;
        this.stackName = stackName;
        this.brickName = brickName;
        this.oldState = oldState;
    }

    public String getProjectConfigurationId() {
        return projectConfigurationId;
    }

    public String getStackName() {
        return stackName;
    }

    public String getBrickName() {
        return brickName;
    }

    public String getOldState() {
        return oldState;
    }
}
