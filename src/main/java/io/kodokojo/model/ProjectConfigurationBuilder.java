package io.kodokojo.model;

import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectConfigurationBuilder {


    private  String identifier;

    private  String entityIdentifier;

    private  String name;

    private  List<User> admins;

    private  Set<StackConfiguration> stackConfigurations;

    private  List<User> users;

    public ProjectConfigurationBuilder(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration != null) {
            identifier = projectConfiguration.getIdentifier();
            entityIdentifier = projectConfiguration.getEntityIdentifier();
            name = projectConfiguration.getName();
            admins = IteratorUtils.toList(projectConfiguration.getAdmins());
            users = IteratorUtils.toList(projectConfiguration.getUsers());
            stackConfigurations = new HashSet<>(projectConfiguration.getStackConfigurations());
        } else {
            admins = new ArrayList<>();
            users = new ArrayList<>();
            stackConfigurations = new HashSet<>();

        }
    }

    public ProjectConfiguration build() {
        return new ProjectConfiguration(entityIdentifier, identifier, name, admins, stackConfigurations, users);
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAdmins(List<User> admins) {
        this.admins = admins;
    }

    public void setStackConfigurations(Set<StackConfiguration> stackConfigurations) {
        this.stackConfigurations = stackConfigurations;
    }

    public ProjectConfigurationBuilder addStackConfiguration(StackConfiguration stackConfiguration) {
        this.stackConfigurations.add(stackConfiguration);
        return this;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
