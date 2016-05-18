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



import io.kodokojo.model.*;

import java.util.List;

/**
 * Allow to manage project.
 */
public interface ProjectManager {

    /**
     * Allow to generate information required to bootstrap a project stack.
     * @param projectName The name of the project to bootstrap
     * @param stackName The name of stack to bootstrap
     * @param stackType The typ of stack
     * @return The bootstrap information's generated.
     */
    BootstrapStackData bootstrapStack(String projectName,String stackName, StackType stackType);

    /**
     * Start all stack and brick for a given ProjectConfiguration.
     * @param projectConfiguration The project configuration to start.
     * @return A snapshot state of Project
     * @throws ProjectAlreadyExistException throw if Project already exist.
     */
    Project start(ProjectConfiguration projectConfiguration) throws ProjectAlreadyExistException;

    /**
     * Add a list of users to a project
     * @param projectConfiguration
     * @param usersToAdd
     */
    void addUsersToProject(ProjectConfiguration projectConfiguration, List<User> usersToAdd);
}
