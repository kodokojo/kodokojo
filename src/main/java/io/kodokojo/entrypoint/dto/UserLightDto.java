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
package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.User;

import java.io.Serializable;

public class UserLightDto implements Serializable {

    private String identifier;

    private String username;

    public UserLightDto(String identifier, String username) {
        this.identifier = identifier;
        this.username = username;
    }

    public UserLightDto(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must be defined.");
        }
        this.identifier = user.getIdentifier();
        this.username = user.getUsername();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = this.identifier;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserLightDto{" +
                "identifier='" + identifier + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
