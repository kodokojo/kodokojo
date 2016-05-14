package io.kodokojo.entrypoint.dto;

public class UserProjectConfigIdDto {

    private String projectConfigurationId;

    private String projectId;

    public UserProjectConfigIdDto(String projectConfigurationId) {
        this.projectConfigurationId = projectConfigurationId;
    }

    public String getProjectConfigurationId() {
        return projectConfigurationId;
    }

    public void setProjectConfigurationId(String projectConfigurationId) {
        this.projectConfigurationId = projectConfigurationId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "UserProjectConfigIdDto{" +
                "projectConfigurationId='" + projectConfigurationId + '\'' +
                ", projectId='" + projectId + '\'' +
                '}';
    }
}
