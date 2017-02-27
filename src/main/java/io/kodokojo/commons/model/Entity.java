/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Entity implements Serializable {

    private final String identifier;

    private final String name;

    private final boolean concrete;

    private final List<ProjectConfiguration> projectConfigurations;

    private final List<User> admins;

    private final List<User> users;

    public Entity(String identifier, String name, boolean concrete, List<ProjectConfiguration> projectConfigurations, List<User> admins, List<User> users) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (projectConfigurations == null) {
            throw new IllegalArgumentException("projectConfigurations must be defined.");
        }
        if (admins == null) {
            throw new IllegalArgumentException("admins must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        this.identifier = identifier;
        this.name = name;
        this.concrete = concrete;
        this.projectConfigurations = projectConfigurations;
        this.admins = admins;
        this.users = users;
    }


    public Entity(String name, boolean concrete) {
        this(null, name, concrete, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Entity(String name) {
        this(null, name, false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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

    public Iterator<ProjectConfiguration> getProjectConfigurations() {
        return projectConfigurations.iterator();
    }

    public Iterator<User> getAdmins() {
        return admins.iterator();
    }

    public Iterator<User> getUsers() {
        return users.iterator();
    }

    public boolean userIsAdmin(String userId) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("userId must be defined.");
        }
        boolean res = false;
        Iterator<User> userIterator = admins.iterator();
        while (!res && userIterator.hasNext()) {
            res = userIterator.next().getIdentifier().equals(userId);
        }
        return res;
    }

    public boolean userIsUserOfEntity(String userId) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("userId must be defined.");
        }
        boolean res = false;
        Iterator<User> userIterator = users.iterator();
        while (!res && userIterator.hasNext()) {
            res = userIterator.next().getIdentifier().equals(userId);
        }
        if (!res) {
            res = userIsAdmin(userId);
        }
        return res;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return identifier.equals(entity.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "Entity{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", concrete=" + concrete +
                ", admins=" + admins +
                ", users=" + users +
                ", projectConfigurations=" + projectConfigurations +
                '}';
    }
}
