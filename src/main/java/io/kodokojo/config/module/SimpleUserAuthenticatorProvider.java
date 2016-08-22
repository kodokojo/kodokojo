/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.config.module;

import com.google.inject.Provider;
import io.kodokojo.service.repository.UserRepository;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.authentification.SimpleUserAuthenticator;
import io.kodokojo.endpoint.UserAuthenticator;

import javax.inject.Inject;

public class SimpleUserAuthenticatorProvider implements Provider<UserAuthenticator<SimpleCredential>> {

    private final UserRepository userRepository;

    @Inject
    public SimpleUserAuthenticatorProvider(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        this.userRepository = userRepository;
    }

    @Override
    public UserAuthenticator<SimpleCredential> get() {
        return new SimpleUserAuthenticator(userRepository);
    }
}
