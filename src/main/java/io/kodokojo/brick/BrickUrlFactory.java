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
package io.kodokojo.brick;

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.ProjectConfiguration;

import static org.apache.commons.lang.StringUtils.isBlank;

public interface BrickUrlFactory {

    String forgeUrl(String entity, String projectName, String stackName, String brickType, String brickName);

    default String forgeUrl(String projectName, String stackName, String brickType, String brickName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(brickType)) {
            throw new IllegalArgumentException("brickType must be defined.");
        }
        if (isBlank(brickName)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        return forgeUrl(null, projectName.toLowerCase(), null, brickType.toLowerCase(), brickName);
    }

    default String forgeUrl(ProjectConfiguration projectConfiguration,  String stackName, BrickConfiguration brickConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        return forgeUrl(projectConfiguration.getName().toLowerCase(),stackName, brickConfiguration.getType().name().toLowerCase(), brickConfiguration.getBrick().getName().toLowerCase());
    }

}
