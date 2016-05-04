package io.kodokojo.model;

/*
 * #%L
 * kodokojo-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfiguration implements Configuration, Cloneable, Serializable {

    private final String identifier;

    private final String entityIdentifier;

    private final String name;

    private final List<User> admins;

    private final Set<StackConfiguration> stackConfigurations;

    private final List<User> users;

    private  String version;

    private  Date versionDate;

    public ProjectConfiguration(String entityIdentifier, String identifier, String name, List<User> admins, Set<StackConfiguration> stackConfigurations, List<User> users) {
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
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
        this.admins = admins;
        this.stackConfigurations = stackConfigurations;
        this.users = users;
    }

    public ProjectConfiguration(String entityIdentifier, String name, List<User> admins, Set<StackConfiguration> stackConfigurations, List<User> users) {
        this(entityIdentifier, null,name, admins, stackConfigurations, users);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
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

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Date getVersionDate() {
        return versionDate;
    }

    @Override
    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
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
    protected Object clone() throws CloneNotSupportedException {
        return new ProjectConfiguration(entityIdentifier, name, admins, new HashSet<>(stackConfigurations), new ArrayList<>(users));
    }


}
