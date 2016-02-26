package io.kodokojo.config.module;

import com.google.inject.Provider;
import io.kodokojo.user.SimpleCredential;
import io.kodokojo.user.SimpleUserAuthenticator;
import io.kodokojo.user.UserAuthenticator;
import io.kodokojo.user.UserManager;

import javax.inject.Inject;

public class SimpleUserAuthenticatorProvider implements Provider<UserAuthenticator<SimpleCredential>> {

    private final UserManager userManager;

    @Inject
    public SimpleUserAuthenticatorProvider(UserManager userManager) {
        if (userManager == null) {
            throw new IllegalArgumentException("userManager must be defined.");
        }
        this.userManager = userManager;
    }

    @Override
    public UserAuthenticator<SimpleCredential> get() {
        return new SimpleUserAuthenticator(userManager);
    }
}
