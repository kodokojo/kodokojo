package io.kodokojo.project;

import io.kodokojo.commons.project.model.Project;
import io.kodokojo.commons.project.model.ProjectConfiguration;

public interface ProjectLauncher {

    Project createOrUpdate(ProjectConfiguration projectConfiguration);

}
