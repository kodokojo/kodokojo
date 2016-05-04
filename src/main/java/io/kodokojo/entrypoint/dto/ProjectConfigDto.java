package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.ProjectConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProjectConfigDto implements Serializable {

    private String name;

    private String identifier;

    private List<UserDto> admins;

    private List<UserDto> users;

    private List<StackConfigDto> stackConfigs;

    public ProjectConfigDto(String name, String identifier, List<UserDto> admins, List<UserDto> users) {
        this.name = name;
        this.identifier = identifier;
        this.admins = admins;
        this.users = users;
    }

    public ProjectConfigDto(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        this.name = projectConfiguration.getName();
        this.identifier = projectConfiguration.getIdentifier();
        this.admins = new ArrayList<>();
        projectConfiguration.getAdmins().forEachRemaining(admin -> admins.add(new UserDto(admin)));
        this.users = new ArrayList<>();
        this.stackConfigs = new ArrayList<>(projectConfiguration.getStackConfigurations().size());
        projectConfiguration.getUsers().forEachRemaining(user -> users.add(new UserDto(user)));
        projectConfiguration.getStackConfigurations().forEach(stackConfiguration -> stackConfigs.add(new StackConfigDto(stackConfiguration)));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserDto> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserDto> admins) {
        this.admins = admins;
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
                ", name='" + name + '\'' +
                ", admins=" + admins +
                ", users=" + users +
                ", stackConfigs=" + stackConfigs +
                '}';
    }
}
