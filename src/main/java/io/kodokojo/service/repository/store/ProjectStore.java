package io.kodokojo.service.repository.store;

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface ProjectStore {

    ProjectConfigurationStoreModel getProjectConfigurationById(String identifier);

    Project getProjectByIdentifier(String identifier);

    Set<String> getProjectConfigIdsByUserIdentifier(String userIdentifier);

    String getProjectIdByProjectConfigurationId(String projectConfigurationId);

    Project getProjectByProjectConfigurationId(String projectConfigurationId);

    boolean projectNameIsValid(String projectName);

    String addProjectConfiguration(ProjectConfigurationStoreModel projectConfiguration);

    String addProject(Project project, String projectConfigurationIdentifier);

    void updateProject(Project project);

    void updateProjectConfiguration(ProjectConfigurationStoreModel projectConfiguration);

    void setContextToBrickConfiguration(String projectConfigurationId, BrickConfiguration brickConfiguration, Map<String, Serializable> context);
}
