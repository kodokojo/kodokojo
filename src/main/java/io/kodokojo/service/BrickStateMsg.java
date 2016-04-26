package io.kodokojo.service;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickStateMsg {

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

    public final String brickName;

    public final State state;

    public BrickStateMsg(String projectConfigurationIdentifier, String brickType, String brickName, State state) {
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        if (isBlank(brickType)) {
            throw new IllegalArgumentException("brickType must be defined.");
        }
        if (isBlank(brickName)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must be defined.");
        }
        this.projectConfigurationIdentifier = projectConfigurationIdentifier;
        this.brickType = brickType;
        this.brickName = brickName;
        this.state = state;
    }

    public String getProjectConfigurationIdentifier() {
        return projectConfigurationIdentifier;
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

    @Override
    public String toString() {
        return "BrickStateMsg{" +
                "projectConfigurationIdentifier='" + projectConfigurationIdentifier + '\'' +
                ", brickType='" + brickType + '\'' +
                ", brickName='" + brickName + '\'' +
                ", state=" + state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrickStateMsg that = (BrickStateMsg) o;

        if (!projectConfigurationIdentifier.equals(that.projectConfigurationIdentifier)) return false;
        if (!brickType.equals(that.brickType)) return false;
        if (!brickName.equals(that.brickName)) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = projectConfigurationIdentifier.hashCode();
        result = 31 * result + brickType.hashCode();
        result = 31 * result + brickName.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}
