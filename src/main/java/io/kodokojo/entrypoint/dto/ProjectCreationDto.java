package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.ProjectConfiguration;

import java.io.Serializable;
import java.util.List;

public class ProjectCreationDto implements Serializable {

    private String name;

    private String ownerIdentifier;

    private List<String> userIdentifiers;

    public ProjectCreationDto(String name, String ownerIdentifier, List<String> userIdentifiers) {
        this.name = name;
        this.ownerIdentifier = ownerIdentifier;
        this.userIdentifiers = userIdentifiers;
    }

    public String getOwnerIdentifier() {
        return ownerIdentifier;
    }

    public void setOwnerIdentifier(String ownerIdentifier) {
        this.ownerIdentifier = ownerIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUserIdentifiers() {
        return userIdentifiers;
    }

    public void setUserIdentifiers(List<String> userIdentifiers) {
        this.userIdentifiers = userIdentifiers;
    }

    @Override
    public String toString() {
        return "ProjectCreationDto{" +
                "name='" + name + '\'' +
                ", ownerIdentifier='" + ownerIdentifier + '\'' +
                ", userIdentifiers=" + userIdentifiers +
                '}';
    }
}
