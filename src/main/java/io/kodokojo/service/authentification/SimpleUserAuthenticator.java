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
package io.kodokojo.service.authentification;

import io.kodokojo.model.User;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.service.repository.UserRepository;

import javax.inject.Inject;

public class SimpleUserAuthenticator implements UserAuthenticator<SimpleCredential> {

    private final UserRepository userRepository;

    @Inject
    public SimpleUserAuthenticator(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        this.userRepository = userRepository;
    }

    @Override
    public User authenticate(SimpleCredential credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException("credentials must be defined.");
        }
        User user = userRepository.getUserByUsername(credentials.getUsername());
        return (user != null && user.getPassword().equals(credentials.getPassword())) ? user : null;
    }

}
