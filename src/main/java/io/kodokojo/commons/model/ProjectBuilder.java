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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectBuilder {

    private String identifier;

    private final String projectConfigurationIdentifier;

    private final String name;

    private Date snapshotDate;

    private Set<Stack> stacks;

    public ProjectBuilder(String projectConfigurationIdentifier, String name) {
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        this.projectConfigurationIdentifier = projectConfigurationIdentifier;
        this.name = name;
        this.stacks = new HashSet<>();
    }

    public ProjectBuilder(Project project) {
        this(project.getProjectConfigurationIdentifier(), project.getName());
        this.identifier = project.getIdentifier();
        this.snapshotDate = project.getSnapshotDate();
        this.stacks  = project.getStacks();
    }

    public Project build() {
        return new Project(identifier, projectConfigurationIdentifier , name, snapshotDate, stacks);
    }

    public ProjectBuilder setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public ProjectBuilder setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
        return this;
    }

    public ProjectBuilder setStacks(Set<Stack> stacks) {
        this.stacks = stacks;
        return this;
    }
}
