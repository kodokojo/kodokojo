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
package io.kodokojo.commons.model;

import org.apache.commons.lang.StringUtils;

import static java.util.Objects.requireNonNull;

public class UserBuilder {

    private  String identifier;

    private  String entityIdentifier;

    private  String firstName;

    private  String lastName;

    private  String name;

    private  String username;

    private  String email;

    private  String password;

    private  String sshPublicKey;

    public UserBuilder() {
        super();
    }

    public UserBuilder(User user) {
        requireNonNull(user, "user must be defined.");
        this.identifier = user.getIdentifier();
        this.entityIdentifier = user.getEntityIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.sshPublicKey = user.getSshPublicKey();
    }

    public User build() {
        return new User(identifier, entityIdentifier, firstName, lastName, username, email, password, sshPublicKey);
    }

    public UserBuilder setIdentifier(String identifier) {
        if (StringUtils.isNotBlank(identifier)) {
            this.identifier = identifier;
        }
        return this;
    }

    public UserBuilder setEntityIdentifier(String entityIdentifier) {
        if (StringUtils.isNotBlank(entityIdentifier)) {
            this.entityIdentifier = entityIdentifier;
        }
        return this;
    }

    public UserBuilder setFirstName(String firstName) {
        if (StringUtils.isNotBlank(firstName)) {
            this.firstName = firstName;
        }
        return this;
    }

    public UserBuilder setLastName(String lastName) {
        if (StringUtils.isNotBlank(lastName)) {
            this.lastName = lastName;
        }
        return this;
    }

    public UserBuilder setName(String name) {
        if (StringUtils.isNotBlank(name)) {
            this.name = name;
        }
        return this;
    }

    public UserBuilder setUsername(String username) {
        if (StringUtils.isNotBlank(username)) {
            this.username = username;
        }
        return this;
    }

    public UserBuilder setEmail(String email) {
        if (StringUtils.isNotBlank(email)) {
            this.email = email;
        }
        return this;
    }

    public UserBuilder setPassword(String password) {
        if (StringUtils.isNotBlank(password)) {
            this.password = password;
        }
        return this;
    }

    public UserBuilder setSshPublicKey(String sshPublicKey) {
        if (StringUtils.isNotBlank(sshPublicKey)) {
            this.sshPublicKey = sshPublicKey;
        }
        return  this;
    }
}
