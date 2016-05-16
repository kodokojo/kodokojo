package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.Project;
import io.kodokojo.model.Stack;

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
