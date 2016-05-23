package io.kodokojo.service.store;

import io.kodokojo.model.User;
import io.kodokojo.model.UserService;

public interface UserFetcher {


    User getUserByUsername(String username);

    User getUserByIdentifier(String identifier);

    UserService getUserServiceByName(String name);
}
