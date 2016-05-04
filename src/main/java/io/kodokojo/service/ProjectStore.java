package io.kodokojo.service;

import io.kodokojo.model.Entity;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;

public interface ProjectStore {

    String addEntity(Entity entity);

    Entity getEntityById(String entityIdentifier);

    String getEntityOfUserId(String userIdentifier);

    boolean projectNameIsValid(String projectName);

    String addProjectConfiguration(ProjectConfiguration projectConfiguration);

    ProjectConfiguration getProjectConfigurationById(String identifier);

    String addProject(Project project);

    Project getProjectByName(String name);

    void updateProjectConfiguration(ProjectConfiguration projectConfiguration);
}
