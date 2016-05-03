package io.kodokojo.brick;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.kodokojo.model.User;

import java.util.List;

/**
 * Allow to configure a Brick from data contain in a given {@link BrickConfigurerData}.
 */
public interface BrickConfigurer {

    /**
     * This step may remove default admin password, configure security policy, etc...
     * @param brickConfigurerData All data which may useful to configure the Brick, like th admin configuration or Brick endpoint.
     * @return The configurationData which may be modify when configuring the Brick.
     */
    BrickConfigurerData configure(BrickConfigurerData brickConfigurerData) throws BrickConfigurationException;

    /**
     * Add users with informations defined in ConfigurationData on the given Brick.
     * @param brickConfigurerData All data which may useful to configure the Brick, like th admin configuration or Brick endpoint.
     * @param users The list of users to add.
     * @return The configurationData which may be modify when adding users.
     */
    BrickConfigurerData addUsers(BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException;

}
