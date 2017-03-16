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


import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class User implements Serializable {

    @Expose
    private final String identifier;

    @Expose
    private final Set<String> organisationIds;

    @Expose
    private final String firstName;

    @Expose
    private final String lastName;

    @Expose
    private final String name;

    @Expose
    private final String username;

    @Expose
    private final String email;

    private final String password;

    @Expose
    private final String sshPublicKey;

    @Expose
    private final boolean root;

    public User(String identifier, Set<String> organisationIds, String firstName, String lastName, String username, String email, String password, String sshPublicKey, boolean root) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        if (firstName == null) {
            throw new IllegalArgumentException("firstName must be defined.");
        }
        if (isBlank(lastName)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        this.identifier = identifier;
        this.organisationIds = organisationIds;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.name = this.firstName + (isNotBlank(this.firstName) ? " " : "") + this.lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sshPublicKey = sshPublicKey;
        this.root = root;
    }

    public User(String identifier, Set<String> organisationIds, String name, String username, String email, String password, String sshPublicKey, boolean isRoot) {
        this(identifier, organisationIds, (name.contains(" ") ? name.substring(0,name.lastIndexOf(" ")): name), (name.contains(" ") ?name.substring(name.lastIndexOf(" "), name.length()): name), username, email, password, sshPublicKey, isRoot);
    }

    public User(String identifier, String entityIdentifier, String name, String username, String email, String password, String sshPublicKey) {
        this(identifier, Collections.singleton(entityIdentifier), name, username, email, password, sshPublicKey, false);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Set<String> getOrganisationIds() {
        return new HashSet<>(organisationIds);
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "User{" +
                "identifier='" + identifier + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + (password != null ? "DEFINED" : "NOT DEFINED") + '\'' +
                ", sshPublicKey='" + (sshPublicKey != null ? "DEFINED" : "NOT DEFINED") + '\'' +
                ", root='" + root + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (identifier != null ? !identifier.equals(user.identifier) : user.identifier != null) return false;
        return username.equals(user.username);

    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + username.hashCode();
        return result;
    }
}
