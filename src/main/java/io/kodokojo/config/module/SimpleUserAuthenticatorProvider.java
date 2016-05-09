package io.kodokojo.config.module;

import com.google.inject.Provider;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.user.SimpleCredential;
import io.kodokojo.service.user.SimpleUserAuthenticator;
import io.kodokojo.entrypoint.UserAuthenticator;

import javax.inject.Inject;

public class SimpleUserAuthenticatorProvider implements Provider<UserAuthenticator<SimpleCredential>> {

    private final UserStore userStore;

    @Inject
    public SimpleUserAuthenticatorProvider(UserStore userStore) {
        if (userStore == null) {
            throw new IllegalArgumentException("userStore must be defined.");
        }
        this.userStore = userStore;
    }

    @Override
    public UserAuthenticator<SimpleCredential> get() {
        return new SimpleUserAuthenticator(userStore);
    }
}
