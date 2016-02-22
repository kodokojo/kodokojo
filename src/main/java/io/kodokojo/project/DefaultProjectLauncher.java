package io.kodokojo.project;

import io.kodokojo.commons.project.model.*;
import io.kodokojo.commons.project.model.Stack;
import io.kodokojo.project.launcher.ConfigurationApplier;
import io.kodokojo.user.UserManager;

import java.security.interfaces.RSAPrivateKey;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultProjectLauncher implements ProjectLauncher {

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
