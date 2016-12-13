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
package io.kodokojo.commons.model;


import com.google.gson.annotations.Expose;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfiguration implements Cloneable, Serializable {

    @Expose
    private final String identifier;

    @Expose
    private final String entityIdentifier;

    @Expose
    private final String name;

    @Expose
    private final UserService userService;

    @Expose
    private final List<User> admins;

    @Expose
    private final Set<StackConfiguration> stackConfigurations;

    @Expose
    private final List<User> users;

    public ProjectConfiguration(String entityIdentifier, String identifier, String name,UserService userService,  List<User> admins, Set<StackConfiguration> stackConfigurations, List<User> users) {
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (userService == null) {
            throw new IllegalArgumentException("userService must be defined.");
        }
        if (CollectionUtils.isEmpty(admins)) {
            throw new IllegalArgumentException("admins must be defined.");
        }
        if (CollectionUtils.isEmpty(stackConfigurations)) {
            throw new IllegalArgumentException("stackConfigurations must be defined and contain some values.");
        }

        this.identifier = identifier;
        this.entityIdentifier = entityIdentifier;
        this.name = name;
        this.userService = userService;
        this.admins = admins;
        this.stackConfigurations = stackConfigurations;
        this.users = users;
    }

    public ProjectConfiguration(String entityIdentifier, String name, UserService userService,List<User> admins, Set<StackConfiguration> stackConfigurations, List<User> users) {
        this(entityIdentifier, null,name, userService, admins, stackConfigurations, users);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public UserService getUserService() {
        return userService;
    }

    public Iterator<User> getAdmins() {
        return admins.iterator();
    }

    public String getName() {
        return name;
    }

    public Set<StackConfiguration> getStackConfigurations() {
        return new HashSet<>(stackConfigurations);
    }

    public Iterator<User> getUsers() {
        return users.iterator();
    }

    public void setUsers(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
    }

    public StackConfiguration getDefaultStackConfiguration() {
        return stackConfigurations.iterator().next();
    }

    public Iterator<BrickConfiguration> getDefaultBrickConfigurations() {
        return getDefaultStackConfiguration().getBrickConfigurations().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectConfiguration that = (ProjectConfiguration) o;

        if (!identifier.equals(that.identifier)) return false;
        if (entityIdentifier != null ? !entityIdentifier.equals(that.entityIdentifier) : that.entityIdentifier != null)
            return false;
        if (!name.equals(that.name)) return false;
        if (!admins.equals(that.admins)) return false;
        if (!stackConfigurations.equals(that.stackConfigurations)) return false;
        return users.equals(that.users);

    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + (entityIdentifier != null ? entityIdentifier.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + admins.hashCode();
        result = 31 * result + stackConfigurations.hashCode();
        result = 31 * result + users.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ProjectConfiguration{" +
                "identifier='" + identifier + '\'' +
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", name='" + name + '\'' +
                ", userService=" + userService +
                ", admins=" + admins +
                ", stackConfigurations=" + stackConfigurations +
                ", users=" + users +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ProjectConfiguration(entityIdentifier, name, userService, admins, new HashSet<>(stackConfigurations), new ArrayList<>(users));
    }


}
