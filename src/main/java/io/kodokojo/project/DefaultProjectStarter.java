package io.kodokojo.project;

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

import io.kodokojo.commons.model.*;
import io.kodokojo.commons.model.Stack;
import io.kodokojo.commons.project.model.*;
import io.kodokojo.user.UserManager;

import java.security.interfaces.RSAPrivateKey;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultProjectStarter implements ProjectStarter {

    private RSAPrivateKey key;

    private UserManager userManager;

    @Override
    public Project createOrUpdate(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }

        List<User> userAdded = projectConfiguration.getUsers().stream().map(user -> {
            if (userManager.addUser(user)) {
                return user;
            }
            return null;
        }).filter(user -> user != null).collect(Collectors.toList());

        Set<Stack> stacks = new HashSet<>();
        for (StackConfiguration stackConfiguration : projectConfiguration.getStackConfigurations()) {
            if (stackConfiguration.getType().equals(StackType.BUILD)) {

                //stacks.add(stack);
            } else {
                throw new UnsupportedOperationException("Not yes able to manage other stack than BUILD.");
            }
        }
        return new Project(projectConfiguration.getName(), new Date(), stacks);
    }


}
