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
package io.kodokojo.model;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickState implements Serializable {

    public enum State {
        STARTING,
        CONFIGURING,
        RUNNING,
        ALREADYEXIST,
        ONFAILURE,
        STOPPED
    }

    public final String projectConfigurationIdentifier;

    public final String brickType;

    public final String stackName;

    public final String brickName;

    public final State state;

    public final String url;

    public final String message;

    public BrickState(String projectConfigurationIdentifier, String stackName, String brickType, String brickName, State state, String url, String message) {
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        if (isBlank(brickType)) {
            throw new IllegalArgumentException("brickType must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        if (isBlank(brickName)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must be defined.");
        }
        this.projectConfigurationIdentifier = projectConfigurationIdentifier;
        this.brickType = brickType;
        this.stackName = stackName;
        this.brickName = brickName;
        this.state = state;
        this.url = url;
        this.message = message;
    }

    public BrickState(String projectConfigurationIdentifier, String stackName, String brickType, String brickName, State state, String url) {
        this(projectConfigurationIdentifier,stackName,  brickType, brickName, state, url, null);
    }

    public BrickState(String projectConfigurationIdentifier, String stackName, String brickType, String brickName, State state) {
        this(projectConfigurationIdentifier,stackName,  brickType, brickName, state, null, null);
    }

    public String getProjectConfigurationIdentifier() {
        return projectConfigurationIdentifier;
    }

    public String getStackName() {
        return stackName;
    }

    public String getBrickType() {
        return brickType;
    }

    public String getBrickName() {
        return brickName;
    }

    public State getState() {
        return state;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BrickState{" +
                "projectConfigurationIdentifier='" + projectConfigurationIdentifier + '\'' +
                ", stackName='" + stackName + '\'' +
                ", brickType='" + brickType + '\'' +
                ", brickName='" + brickName + '\'' +
                ", message=" + message +
                ", url=" + url +
                ", state=" + state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrickState that = (BrickState) o;

        if (!projectConfigurationIdentifier.equals(that.projectConfigurationIdentifier)) return false;
        if (!brickType.equals(that.brickType)) return false;
        if (!brickName.equals(that.brickName)) return false;
        if (!stackName.equals(that.stackName)) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = projectConfigurationIdentifier.hashCode();
        result = 31 * result + brickType.hashCode();
        result = 31 * result + brickName.hashCode();
        result = 31 * result + stackName.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}
