package io.kodokojo.service.store;

import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;

import java.util.Set;

public interface ProjectFetcher {

    ProjectConfiguration getProjectConfigurationById(String identifier);

    Project getProjectByIdentifier(String identifier);

    Set<String> getProjectConfigIdsByUserIdentifier(String userIdentifier);

    String getProjectIdByProjectConfigurationId(String projectConfigurationId);

    Project getProjectByProjectConfigurationId(String projectConfigurationId);
}
