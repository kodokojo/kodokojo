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
package io.kodokojo.model;

import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectConfigurationBuilder {


    private String identifier;

    private String entityIdentifier;

    private String name;

    private UserService userService;

    private List<User> admins;

    private Set<StackConfiguration> stackConfigurations;

    private List<User> users;

    public ProjectConfigurationBuilder(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration != null) {
            identifier = projectConfiguration.getIdentifier();
            entityIdentifier = projectConfiguration.getEntityIdentifier();
            name = projectConfiguration.getName();
            userService = projectConfiguration.getUserService();
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
        return new ProjectConfiguration(entityIdentifier, identifier, name, userService, admins, stackConfigurations, users);
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
