package io.kodokojo.service.user;

import io.kodokojo.model.User;
import io.kodokojo.entrypoint.UserAuthenticator;
import io.kodokojo.service.store.UserStore;

import javax.inject.Inject;

public class SimpleUserAuthenticator implements UserAuthenticator<SimpleCredential> {

    private final UserStore userStore;

    @Inject
    public SimpleUserAuthenticator(UserStore userStore) {
        if (userStore == null) {
            throw new IllegalArgumentException("userStore must be defined.");
        }
        this.userStore = userStore;
    }

    @Override
    public User authenticate(SimpleCredential credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException("credentials must be defined.");
        }
        User user = userStore.getUserByUsername(credentials.getUsername());
        return (user != null && user.getPassword().equals(credentials.getPassword())) ? user : null;
    }

}
