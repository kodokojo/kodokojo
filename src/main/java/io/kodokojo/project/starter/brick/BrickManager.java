package io.kodokojo.project.starter.brick;

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.ProjectConfiguration;

public interface BrickManager {

    void start(ProjectConfiguration projectConfiguration, BrickConfiguration brickConfiguration);

}
