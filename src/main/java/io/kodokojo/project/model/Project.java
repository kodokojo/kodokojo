package io.kodokojo.project.model;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Project {

    private final String name;

    private final String ownerEmail;

    private final Set<Stack> stacks;

    public Project(String name, String ownerEmail, Set<Stack> stacks) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(ownerEmail)) {
            throw new IllegalArgumentException("ownerEmail must be defined.");
        }
        if (CollectionUtils.isEmpty(stacks)) {
            throw new IllegalArgumentException("stacks must be defined and contain some values.");
        }
        this.name = name;
        this.ownerEmail = ownerEmail;
        this.stacks = stacks;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getName() {
        return name;
    }

    public Set<Stack> getStacks() {
        return new HashSet<>(stacks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (!name.equals(project.name)) return false;
        if (!ownerEmail.equals(project.ownerEmail)) return false;
        return stacks.equals(project.stacks);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + ownerEmail.hashCode();
        result = 31 * result + stacks.hashCode();
        return result;
    }
}
