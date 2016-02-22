package io.kodokojo.user;

import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.project.model.UserService;

public interface UserManager {

    boolean addUser(User user);

    boolean addUserService(UserService userService);

    User getUserByUsername(String username);

    UserService getUserServiceByName(String name);

}
