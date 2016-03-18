package io.kodokojo.project.starter;

import io.kodokojo.commons.model.Service;
import io.kodokojo.model.*;

import java.util.Set;

public interface BrickManager {

    Stack.OrchestratorType getOrchestratorType();

    Set<Service> start(ProjectConfiguration projectConfiguration, BrickType brickType);

    void configure(ProjectConfiguration projectConfiguration, BrickType brickType);

    boolean stop(BrickDeploymentState brickDeploymentState);

}
