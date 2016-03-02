package io.kodokojo.project;

import io.kodokojo.commons.model.Project;

public interface ProjectStore {

    void addProject(Project project);

    Project getProjectByName(String name);

}
