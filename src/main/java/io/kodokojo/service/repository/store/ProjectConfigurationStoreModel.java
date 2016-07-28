package io.kodokojo.service.repository.store;

import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;
import io.kodokojo.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectConfigurationStoreModel {

    private final String identifier;

    private final String entityIdentifier;

    private final String name;

    private final List<String> admins;

    private final Set<StackConfiguration> stackConfigurations;

    private final List<String> users;

    public ProjectConfigurationStoreModel(String identifier, String entityIdentifier, String name, List<String> admins, Set<StackConfiguration> stackConfigurations, List<String> users) {
        this.identifier = identifier;
        this.entityIdentifier = entityIdentifier;
        this.name = name;
        this.admins = admins;
        this.stackConfigurations = stackConfigurations;
        this.users = users;
    }

    public ProjectConfigurationStoreModel(ProjectConfiguration projectConfiguration) {
        this.identifier = projectConfiguration.getIdentifier();
        this.entityIdentifier = projectConfiguration.getEntityIdentifier();
        this.name = projectConfiguration.getName();
        this.stackConfigurations = new HashSet<>(projectConfiguration.getStackConfigurations());
        this.admins = new ArrayList<>();
        projectConfiguration.getAdmins().forEachRemaining(a -> this.admins.add(a.getIdentifier()));
        this.users = new ArrayList<>();
        projectConfiguration.getUsers().forEachRemaining(u -> this.users.add(u.getIdentifier()));
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public String getName() {
        return name;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public Set<StackConfiguration> getStackConfigurations() {
        return stackConfigurations;
    }

    public List<String> getUsers() {
        return users;
    }
}
