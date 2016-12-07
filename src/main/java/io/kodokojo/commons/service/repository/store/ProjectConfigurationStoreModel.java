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
package io.kodokojo.commons.service.repository.store;

import io.kodokojo.commons.model.ProjectConfiguration;
import io.kodokojo.commons.model.StackConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectConfigurationStoreModel implements Serializable {

    private final String identifier;

    private final String entityIdentifier;

    private final String name;

    private final String userService;

    private final List<String> admins;

    private final Set<StackConfiguration> stackConfigurations;

    private final List<String> users;

    public ProjectConfigurationStoreModel( String entityIdentifier, String identifier, String name,String userService,  List<String> admins, Set<StackConfiguration> stackConfigurations, List<String> users) {
        this.identifier = identifier;
        this.entityIdentifier = entityIdentifier;
        this.name = name;
        this.userService = userService;
        this.admins = admins;
        this.stackConfigurations = stackConfigurations;
        this.users = users;
    }

    public ProjectConfigurationStoreModel(ProjectConfiguration projectConfiguration) {
        this.identifier = projectConfiguration.getIdentifier();
        this.entityIdentifier = projectConfiguration.getEntityIdentifier();
        this.name = projectConfiguration.getName();
        this.stackConfigurations = new HashSet<>(projectConfiguration.getStackConfigurations());
        this.userService = projectConfiguration.getUserService().getIdentifier();
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

    public String getUserService() {
        return userService;
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
