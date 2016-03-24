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

    private final String name;

    private final User owner;

    private final Set<StackConfiguration> stackConfigurations;

    private final List<User> users;

    private  String version;

    private  Date versionDate;

    public ProjectConfiguration(String identifier, String name, User owner, Set<StackConfiguration> stackConfigurations, List<User> users) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (owner == null) {
            throw new IllegalArgumentException("owner must be defined.");
        }
        if (CollectionUtils.isEmpty(stackConfigurations)) {
            throw new IllegalArgumentException("stackConfigurations must be defined and contain some values.");
        }

        this.identifier = identifier;
        this.name = name;
        this.owner = owner;
        this.stackConfigurations = stackConfigurations;
        this.users = users;
    }

    public ProjectConfiguration( String name, User owner, Set<StackConfiguration> stackConfigurations, List<User> users) {
        this(null,name,owner, stackConfigurations, users);
    }

    public String getIdentifier() {
        return identifier;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Set<StackConfiguration> getStackConfigurations() {
        return new HashSet<StackConfiguration>(stackConfigurations);
    }

    public List<User> getUsers() {
        return users;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectConfiguration projectConfiguration = (ProjectConfiguration) o;

        if (!name.equals(projectConfiguration.name)) return false;
        if (!owner.equals(projectConfiguration.owner)) return false;
        if (!stackConfigurations.equals(projectConfiguration.stackConfigurations)) return false;
        return users.equals(projectConfiguration.users);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + owner.hashCode();
        result = 31 * result + stackConfigurations.hashCode();
        result = 31 * result + users.hashCode();
        return result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ProjectConfiguration(name, owner, new HashSet<StackConfiguration>(stackConfigurations), new ArrayList<User>(users));
    }


}
