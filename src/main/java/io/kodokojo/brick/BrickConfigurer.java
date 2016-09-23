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



import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;

import java.util.List;

/**
 * Allow to configure a BrickConfiguration from data contain in a given {@link BrickConfigurerData}.
 */
public interface BrickConfigurer {

    /**
     * This step may remove default admin password, configure security policy, etc...
     * @param brickConfigurerData All data which may useful to configure the BrickConfiguration, like th admin configuration or BrickConfiguration endpoint.
     * @return The configurationData which may be modify when configuring the BrickConfiguration.
     */
    BrickConfigurerData configure(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData) throws BrickConfigurationException;

    /**
     * Add users with informations defined in ConfigurationData on the given BrickConfiguration.
     * @param brickConfigurerData All data which may useful to configure the BrickConfiguration, like th admin configuration or BrickConfiguration endpoint.
     * @param users The list of users to add.
     * @return The configurationData which may be modify when adding users.
     */
    BrickConfigurerData addUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException;

    BrickConfigurerData removeUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurationData, List<User> users);
}
