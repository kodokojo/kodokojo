/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
