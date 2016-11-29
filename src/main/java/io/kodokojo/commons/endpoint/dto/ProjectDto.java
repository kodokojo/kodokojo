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
package io.kodokojo.commons.endpoint.dto;

import io.kodokojo.commons.model.Project;
import io.kodokojo.commons.model.Stack;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class ProjectDto implements Serializable {

    private String identifier;

    private String projectConfigurationIdentifier;

    private String name;

    private Date snapshotDate;

    private Set<Stack> stacks;

    public ProjectDto(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        this.identifier = project.getIdentifier();
        this.projectConfigurationIdentifier = project.getProjectConfigurationIdentifier();
        this.name = project.getName();
        this.snapshotDate = project.getSnapshotDate();
        this.stacks = project.getStacks();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getProjectConfigurationIdentifier() {
        return projectConfigurationIdentifier;
    }

    public void setProjectConfigurationIdentifier(String projectConfigurationIdentifier) {
        this.projectConfigurationIdentifier = projectConfigurationIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Set<Stack> getStacks() {
        return stacks;
    }

    public void setStacks(Set<Stack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public String toString() {
        return "ProjectDto{" +
                "identifier='" + identifier + '\'' +
                ", projectConfigurationIdentifier='" + projectConfigurationIdentifier + '\'' +
                ", name='" + name + '\'' +
                ", snapshotDate=" + snapshotDate +
                ", stacks=" + stacks +
                '}';
    }
}
