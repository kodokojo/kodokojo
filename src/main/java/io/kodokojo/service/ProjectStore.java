package io.kodokojo.service;

import io.kodokojo.model.Project;

public interface ProjectStore {

    boolean projectNameIsValid(String projectName);

    void addProject(Project project);

    Project getProjectByName(String name);

}
