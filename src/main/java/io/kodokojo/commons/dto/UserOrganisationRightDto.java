package io.kodokojo.commons.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserOrganisationRightDto implements Serializable {

    private String identifier;

    private String name;

    private Right right;

    private List<UserProjectConfigurationRightDto> projectConfigurations;

    public enum Right {
        ADMIN,
        USER
    }

    public UserOrganisationRightDto() {
        super();
        projectConfigurations = new ArrayList<>();
    }

    public UserOrganisationRightDto(String identifier, String name, Right right, List<UserProjectConfigurationRightDto> projectConfigurations) {
        this.identifier = identifier;
        this.name = name;
        this.right = right;
        this.projectConfigurations = projectConfigurations;
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

    public Right getRight() {
        return right;
    }

    public void setRight(Right right) {
        this.right = right;
    }

    public List<UserProjectConfigurationRightDto> getProjectConfigurations() {
        return projectConfigurations;
    }

    public void setProjectConfigurations(List<UserProjectConfigurationRightDto> projectConfigurations) {
        this.projectConfigurations = projectConfigurations;
    }

    @Override
    public String toString() {
        return "UserOrganisationRightDto{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", right=" + right +
                ", projectConfigurations=" + projectConfigurations +
                '}';
    }
}
