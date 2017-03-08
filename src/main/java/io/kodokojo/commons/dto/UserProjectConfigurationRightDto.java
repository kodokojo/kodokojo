package io.kodokojo.commons.dto;

import java.io.Serializable;

public class UserProjectConfigurationRightDto implements Serializable {

    private String identifier;

    private String projectId;

    private String name;

    private boolean isTeamLeader;

    public UserProjectConfigurationRightDto() {
        super();
    }

    public UserProjectConfigurationRightDto(String identifier, String projectId, String name, boolean isTeamLeader) {
        this.identifier = identifier;
        this.projectId = projectId;
        this.name = name;
        this.isTeamLeader = isTeamLeader;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTeamLeader() {
        return isTeamLeader;
    }

    public void setTeamLeader(boolean teamLeader) {
        isTeamLeader = teamLeader;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "UserSoftwareFactoryRightDto{" +
                "identifier='" + identifier + '\'' +
                ", projectId='" + projectId + '\'' +
                ", name='" + name + '\'' +
                ", isTeamLeader=" + isTeamLeader +
                '}';
    }
}
