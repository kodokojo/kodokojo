package io.kodokojo.commons.model;

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

import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfiguration implements Configuration, Cloneable {

    private final String name;

    private final String ownerEmail;

    private final Set<StackConfiguration> stackConfigurations;

    private final List<User> users;

    private  String version;

    private  Date versionDate;

    public ProjectConfiguration(String name, String ownerEmail, Set<StackConfiguration> stackConfigurations, List<User> users) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(ownerEmail)) {
            throw new IllegalArgumentException("ownerEmail must be defined.");
        }
        if (CollectionUtils.isEmpty(stackConfigurations)) {
            throw new IllegalArgumentException("stackConfigurations must be defined and contain some values.");
        }
        if (CollectionUtils.isEmpty(users)) {
            throw new IllegalArgumentException("users must be defined and contain some values.");
        }
        this.name = name;
        this.ownerEmail = ownerEmail;
        this.stackConfigurations = stackConfigurations;
        this.users = users;
    }

    public String getOwnerEmail() {
        return ownerEmail;
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
        if (!ownerEmail.equals(projectConfiguration.ownerEmail)) return false;
        if (!stackConfigurations.equals(projectConfiguration.stackConfigurations)) return false;
        return users.equals(projectConfiguration.users);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + ownerEmail.hashCode();
        result = 31 * result + stackConfigurations.hashCode();
        result = 31 * result + users.hashCode();
        return result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ProjectConfiguration(name, ownerEmail, new HashSet<StackConfiguration>(stackConfigurations), new ArrayList<User>(users));
    }


}
