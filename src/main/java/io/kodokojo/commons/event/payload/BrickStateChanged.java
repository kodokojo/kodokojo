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

public class BrickStateChanged implements Serializable {

    private final String projectConfigurationId;

    private final String stackName;

    private final String brickName;

    private final String oldState;

    public BrickStateChanged(String projectConfigurationId, String stackName, String brickName, String oldState) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        if (isBlank(brickName)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        this.projectConfigurationId = projectConfigurationId;
        this.stackName = stackName;
        this.brickName = brickName;
        this.oldState = oldState;
    }

    public String getProjectConfigurationId() {
        return projectConfigurationId;
    }

    public String getStackName() {
        return stackName;
    }

    public String getBrickName() {
        return brickName;
    }

    public String getOldState() {
        return oldState;
    }
}
