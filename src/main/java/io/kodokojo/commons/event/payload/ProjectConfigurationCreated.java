package io.kodokojo.commons.event.payload;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfigurationCreated implements Serializable {

    private final String projectConfigurationId;

    private final String projectName;

    public ProjectConfigurationCreated(String projectConfigurationId, String projectName) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        this.projectConfigurationId = projectConfigurationId;
        this.projectName = projectName;
    }

    public String getProjectConfigurationId() {
        return projectConfigurationId;
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public String toString() {
        return "ProjectConfigurationCreated{" +
                "projectConfigurationId='" + projectConfigurationId + '\'' +
                ", projectName='" + projectName + '\'' +
                '}';
    }
}
