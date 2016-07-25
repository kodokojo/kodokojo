/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service;

import io.kodokojo.brick.BrickAlreadyExist;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerProvider;
import io.kodokojo.model.Service;
import io.kodokojo.model.*;

import java.util.Set;

/**
 * Allow to manage Brick throw a {@link ProjectConfiguration}, {@link BrickDeploymentState} and {@link BrickType}.
 */
public interface BrickManager {

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
     * This step may lookup a {@link BrickConfigurer} from a {@link BrickConfigurerProvider} and apply it.</br>
     * This step may also add all users defined in ProjectConfiguration.
     * @param projectConfiguration The projectConfiguration which contain all data required to configure Brick.
     * @param brickType The BrickType to start.
     */
    void configure(ProjectConfiguration projectConfiguration, BrickType brickType) throws ProjectConfigurationException;

    /**
     * Stop a given Brick
     * @param brickDeploymentState The state brick to stop.
     * @return <code>true</code> is succefully stopped.
     */
    boolean stop(BrickDeploymentState brickDeploymentState);

}
