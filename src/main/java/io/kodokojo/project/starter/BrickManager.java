package io.kodokojo.project.starter;

import io.kodokojo.commons.model.Service;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickAlreadyExist;

import java.util.Set;

public interface BrickManager {

    Stack.OrchestratorType getOrchestratorType();

    Set<Service> start(ProjectConfiguration projectConfiguration, BrickType brickType) throws BrickAlreadyExist;

    void configure(ProjectConfiguration projectConfiguration, BrickType brickType);

    boolean stop(BrickDeploymentState brickDeploymentState);

}
