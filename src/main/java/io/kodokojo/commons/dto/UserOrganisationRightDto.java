package io.kodokojo.commons.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserOrganisationRightDto implements Serializable {

    private String identifier;

    private String name;

    private Right right;

    private List<UserProjectRightDto> softwareFactories;

    public enum Right {
        ADMIN,
        USER
    }

    public UserOrganisationRightDto() {
        super();
        softwareFactories = new ArrayList<>();
    }

    public UserOrganisationRightDto(String identifier, String name, Right right, List<UserProjectRightDto> softwareFactories) {
        this.identifier = identifier;
        this.name = name;
        this.right = right;
        this.softwareFactories = softwareFactories;
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

    public List<UserProjectRightDto> getSoftwareFactories() {
        return softwareFactories;
    }

    public void setSoftwareFactories(List<UserProjectRightDto> softwareFactories) {
        this.softwareFactories = softwareFactories;
    }

    @Override
    public String toString() {
        return "UserOrganisationRightDto{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", right=" + right +
                ", softwareFactories=" + softwareFactories +
                '}';
    }
}
