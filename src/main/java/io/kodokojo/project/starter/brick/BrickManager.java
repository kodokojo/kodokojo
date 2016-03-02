package io.kodokojo.project.starter.brick;

import io.kodokojo.commons.model.BrickConfiguration;
import io.kodokojo.commons.model.ProjectConfiguration;

public interface BrickManager {

    void start(ProjectConfiguration projectConfiguration, BrickConfiguration brickConfiguration);

}
