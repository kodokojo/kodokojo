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

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Project implements Serializable {

    private final String identifier;

    private final String projectConfigurationIdentifier;

    private final String name;

    private final SSLKeyPair sslRootCaKey;

    private final Date snapshotDate;

    private final Set<Stack> stacks;

    public Project(String identifier, String projectConfigurationIdentifier, String name, SSLKeyPair sslRootCaKey, Date snapshotDate, Set<Stack> stacks) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        if (sslRootCaKey == null) {
            throw new IllegalArgumentException("sslRootCaKey must be defined.");
        }
        if (snapshotDate == null) {
            throw new IllegalArgumentException("snapshotDate must be defined.");
        }
        if (CollectionUtils.isEmpty(stacks)) {
            throw new IllegalArgumentException("stacks must be defined.");
        }
        this.identifier = identifier;
        this.projectConfigurationIdentifier = projectConfigurationIdentifier;
        this.name = name;
        this.sslRootCaKey = sslRootCaKey;
        this.snapshotDate = snapshotDate;
        this.stacks = stacks;
    }

    public Project(String projectConfigurationIdentifier, String name, SSLKeyPair sslRootCaKey, Date snapshotDate, Set<Stack> stacks) {
        this(null, projectConfigurationIdentifier,  name, sslRootCaKey, snapshotDate, stacks);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getProjectConfigurationIdentifier() {
        return projectConfigurationIdentifier;
    }

    public SSLKeyPair getSslRootCaKey() {
        return sslRootCaKey;
    }

    public String getName() {
        return name;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public Set<Stack> getStacks() {
        return stacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (!name.equals(project.name)) return false;
        if (!projectConfigurationIdentifier.equals(project.projectConfigurationIdentifier)) return false;
        if (!snapshotDate.equals(project.snapshotDate)) return false;
        return stacks.equals(project.stacks);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + projectConfigurationIdentifier.hashCode();
        result = 31 * result + snapshotDate.hashCode();
        result = 31 * result + stacks.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                "projectConfigurationIdentifier='" + projectConfigurationIdentifier + '\'' +
                ",identifier='" + identifier + '\'' +
                ", snapshotDate=" + snapshotDate +
                ", stacks=" + stacks +
                '}';
    }
}
