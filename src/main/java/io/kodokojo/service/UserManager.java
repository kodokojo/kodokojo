package io.kodokojo.service;

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
import io.kodokojo.model.UserService;

/**
 * Allow to manage {@link User} and {@link UserService}.
 */
public interface UserManager {

    String generateId();

    boolean identifierExpectedNewUser(String generatedId);

    boolean addUser(User user);

    boolean addUserService(UserService userService);

    User getUserByUsername(String username);

    User getUserByIdentifier(String identifier);

    UserService getUserServiceByName(String name);

}
