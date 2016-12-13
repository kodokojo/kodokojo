package io.kodokojo.commons.event.payload;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class StackStarted implements Serializable {

    private final String projectName;

    private final String stackName;

    public StackStarted(String projectName, String stackName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        this.projectName = projectName;
        this.stackName = stackName;
    }

    public String getStackName() {
        return stackName;
    }

    public String getProjectName() {
        return projectName;
    }
}
