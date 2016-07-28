package io.kodokojo.service.repository.store;

import io.kodokojo.model.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EntityStoreModel implements Serializable {

    private final String identifier;

    private final String name;

    private final boolean concrete;

    private final List<String> projectConfigurations;

    private final List<String> admins;

    private final List<String> users;

    public EntityStoreModel(Entity entity) {
        this.identifier = entity.getIdentifier();
        this.name = entity.getName();
        this.concrete = entity.isConcrete();
        this.projectConfigurations = new ArrayList<>();
        entity.getProjectConfigurations().forEachRemaining(c -> this.projectConfigurations.add(c.getIdentifier()));
        this.admins = new ArrayList<>();
        entity.getAdmins().forEachRemaining(a -> this.admins.add(a.getIdentifier()));
        this.users = new ArrayList<>();
        entity.getUsers().forEachRemaining(u -> this.users.add(u.getIdentifier()));
    }

    public EntityStoreModel(String identifier, String name, boolean concrete, List<String> projectConfigurations, List<String> admins, List<String> users) {
        this.identifier = identifier;
        this.name = name;
        this.concrete = concrete;
        this.projectConfigurations = projectConfigurations;
        this.admins = admins;
        this.users = users;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public boolean isConcrete() {
        return concrete;
    }

    public List<String> getProjectConfigurations() {
        return projectConfigurations;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public List<String> getUsers() {
        return users;
    }
}
