package io.kodokojo.model;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectBuilder {

    private String identifier;

    private final String projectConfigurationIdentifier;

    private final String name;

    private SSLKeyPair sslRootCaKey;

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
        this.sslRootCaKey = project.getSslRootCaKey();
        this.snapshotDate = project.getSnapshotDate();
        this.stacks  = project.getStacks();
    }

    public Project build() {
        return new Project(identifier, projectConfigurationIdentifier , name, sslRootCaKey, snapshotDate, stacks);
    }

    public ProjectBuilder setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public ProjectBuilder setSslRootCaKey(SSLKeyPair sslRootCaKey) {
        this.sslRootCaKey = sslRootCaKey;
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
