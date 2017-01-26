/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.event.payload;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserCreated implements Serializable {

    private final String identifier;

    private final String username;

    private final String email;

    public UserCreated(String identifier, String username, String email) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        if (isBlank(email)) {
            throw new IllegalArgumentException("email must be defined.");
        }
        this.identifier = identifier;
        this.username = username;
        this.email = email;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UserCreated{" +
                "identifier='" + identifier + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
