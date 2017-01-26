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
package io.kodokojo.commons.event.payload;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfigurationCreated implements Serializable {

    private final String projectConfigurationId;

    private final String projectName;

    public ProjectConfigurationCreated(String projectConfigurationId, String projectName) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        this.projectConfigurationId = projectConfigurationId;
        this.projectName = projectName;
    }

    public String getProjectConfigurationId() {
        return projectConfigurationId;
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public String toString() {
        return "ProjectConfigurationCreated{" +
                "projectConfigurationId='" + projectConfigurationId + '\'' +
                ", projectName='" + projectName + '\'' +
                '}';
    }
}
