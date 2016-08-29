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
package io.kodokojo.service.actor.message;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickStateEvent implements Serializable {

    public enum State {
        STARTING,
        CONFIGURING,
        RUNNING,
        ALREADYEXIST,
        ONFAILURE,
        STOPPED
    }

    private final String projectConfigurationIdentifier;

    private final String brickType;

    private final String stackName;

    private final String brickName;

    private final State oldState;

    private final State newState;

    private final String url;

    private final String message;

    private final String version;

    public BrickStateEvent(String projectConfigurationIdentifier, String stackName, String brickType, String brickName, State oldState, State newState, String url, String message, String version) {
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
        if (newState == null) {
            throw new IllegalArgumentException("newState must be defined.");
        }
        if (isBlank(version)) {
            throw new IllegalArgumentException("version must be defined.");
        }
        this.projectConfigurationIdentifier = projectConfigurationIdentifier;
        this.brickType = brickType;
        this.stackName = stackName;
        this.brickName = brickName;
        this.oldState = oldState;
        this.newState = newState;
        this.url = url;
        this.message = message;
        this.version = version;
    }

    public BrickStateEvent(String projectConfigurationIdentifier, String stackName, String brickType, String brickName, State newState, String url, String version) {
        this(projectConfigurationIdentifier,stackName,  brickType, brickName,null, newState, url, null, version);
    }

    public BrickStateEvent(String projectConfigurationIdentifier, String stackName, String brickType, String brickName, State newState, String version) {
        this(projectConfigurationIdentifier,stackName,  brickType, brickName,null, newState, null, null, version);
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
        return newState;
    }

    public State getOldState() {
        return oldState;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "BrickStateEvent{" +
                "projectConfigurationIdentifier='" + projectConfigurationIdentifier + '\'' +
                ", brickType='" + brickType + '\'' +
                ", stackName='" + stackName + '\'' +
                ", brickName='" + brickName + '\'' +
                ", oldState=" + oldState +
                ", newState=" + newState +
                ", url='" + url + '\'' +
                ", message='" + message + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrickStateEvent that = (BrickStateEvent) o;

        if (!projectConfigurationIdentifier.equals(that.projectConfigurationIdentifier)) return false;
        if (!brickType.equals(that.brickType)) return false;
        if (!brickName.equals(that.brickName)) return false;
        if (!stackName.equals(that.stackName)) return false;
        if (!version.equals(that.version)) return false;
        return newState == that.newState;

    }

    @Override
    public int hashCode() {
        int result = projectConfigurationIdentifier.hashCode();
        result = 31 * result + brickType.hashCode();
        result = 31 * result + brickName.hashCode();
        result = 31 * result + stackName.hashCode();
        result = 31 * result + newState.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
