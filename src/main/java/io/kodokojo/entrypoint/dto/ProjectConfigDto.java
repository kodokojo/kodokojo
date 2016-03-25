package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.ProjectConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProjectConfigDto implements Serializable {

    private String identifier;

    private UserDto owner;

    private List<UserDto> users;

    private List<StackConfigDto> stackConfigs;

    public ProjectConfigDto(String identifier, UserDto owner, List<UserDto> users) {
        this.identifier = identifier;
        this.owner = owner;
        this.users = users;
    }

    public ProjectConfigDto(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        this.identifier = projectConfiguration.getIdentifier();
        this.owner = new UserDto(projectConfiguration.getOwner());
        this.users = new ArrayList<>(projectConfiguration.getUsers().size());
        this.stackConfigs = new ArrayList<>(projectConfiguration.getStackConfigurations().size());
        projectConfiguration.getUsers().forEach(user -> users.add(new UserDto(user)));
        projectConfiguration.getStackConfigurations().forEach(stackConfiguration -> stackConfigs.add(new StackConfigDto(stackConfiguration)));
    }

    public UserDto getOwner() {
        return owner;
    }

    public void setOwner(UserDto owner) {
        this.owner = owner;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<StackConfigDto> getStackConfigs() {
        return stackConfigs;
    }

    public void setStackConfigs(List<StackConfigDto> stackConfigs) {
        this.stackConfigs = stackConfigs;
    }

    @Override
    public String toString() {
        return "ProjectConfigDto{" +
                "identifier='" + identifier + '\'' +
                ", owner=" + owner +
                ", users=" + users +
                ", stackConfigs=" + stackConfigs +
                '}';
    }
}
