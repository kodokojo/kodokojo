package io.kodokojo.project.starter.brick;

import io.kodokojo.commons.model.Service;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.BrickEntity;
import io.kodokojo.model.BrickType;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.project.starter.ProjectConfigurer;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

public interface BrickManager {

    Set<Service> start(ProjectConfiguration projectConfiguration, BrickType brickType);

    void configure(ProjectConfiguration projectConfiguration, BrickType brickType);

    boolean stop(BrickEntity brickEntity);

}
