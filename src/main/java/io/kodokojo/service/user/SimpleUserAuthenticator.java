package io.kodokojo.service.user;

import io.kodokojo.model.User;
import io.kodokojo.entrypoint.UserAuthenticator;
import io.kodokojo.service.UserManager;

import javax.inject.Inject;

public class SimpleUserAuthenticator implements UserAuthenticator<SimpleCredential> {

    private final UserManager userManager;

    @Inject
    public SimpleUserAuthenticator(UserManager userManager) {
        if (userManager == null) {
            throw new IllegalArgumentException("userManager must be defined.");
        }
        this.userManager = userManager;
    }

    @Override
    public User authenticate(SimpleCredential credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException("credentials must be defined.");
        }
        User user = userManager.getUserByUsername(credentials.getUsername());
        return (user != null && user.getPassword().equals(credentials.getPassword())) ? user : null;
    }

}
