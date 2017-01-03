/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.model.Project;
import io.kodokojo.commons.model.ProjectConfiguration;

public interface ProjectRepository extends ProjectFetcher {

    boolean projectNameIsValid(String projectName);

    String addProjectConfiguration(ProjectConfiguration projectConfiguration);

    String addProject(Project project, String projectConfigurationIdentifier);

    void updateProject(Project project);

    void updateProjectConfiguration(ProjectConfiguration projectConfiguration);

}
