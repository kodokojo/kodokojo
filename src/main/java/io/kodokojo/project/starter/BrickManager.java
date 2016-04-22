package io.kodokojo.project.starter;

import io.kodokojo.commons.model.Service;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickAlreadyExist;

import java.util.Set;

/**
 * Allow to manage Brick throw a {@link ProjectConfiguration}, {@link BrickDeploymentState} and {@link BrickType}.
 */
public interface BrickManager {

    @Deprecated
    // Not useful, to remove.
    Stack.OrchestratorType getOrchestratorType();

    /**
     * Start a given {@link BrickType} from the Default StackConfiguration defined in ProjectConfiguration.</br>
     * Start operation may take a while, we may introduce a callback to be more reactive.
     * @param projectConfiguration The projectConfiguration which contain all data required to start Brick.
     * @param brickType The BrickType to start.
     * @return A list of started endpoint and ready to be configured.
     * @throws BrickAlreadyExist Throw if Brick had been already started for this Project.
     */
    Set<Service> start(ProjectConfiguration projectConfiguration, BrickType brickType) throws BrickAlreadyExist;

    /**
     * Configure a Brick for thos ProjectConfiguration.</br>
     * This step may lookup a {@link BrickConfigurer} from a {@link io.kodokojo.service.BrickConfigurerProvider} and apply it.</br>
     * This step may also add all users defined in ProjectConfiguration.
     * @param projectConfiguration The projectConfiguration which contain all data required to configure Brick.
     * @param brickType The BrickType to start.
     */
    void configure(ProjectConfiguration projectConfiguration, BrickType brickType);

    /**
     * Stop a given Brick
     * @param brickDeploymentState The state brick to stop.
     * @return <code>true</code> is succefully stopped.
     */
    boolean stop(BrickDeploymentState brickDeploymentState);

}
