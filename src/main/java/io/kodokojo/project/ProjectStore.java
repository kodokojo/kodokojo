package io.kodokojo.project;

import io.kodokojo.commons.project.model.Project;

public interface ProjectStore {

    void addProject(Project project);

    Project getProjectByName(String name);

}
