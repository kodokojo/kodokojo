package io.kodokojo.model;

/*
 * #%L
 * kodokojo-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class User implements Serializable {

    private final String identifier;

    private final String entityIdentifier;

    private final String firstName;

    private final String lastName;

    private final String name;

    private final String username;

    private final String email;

    private final String password;

    private final String sshPublicKey;

    public User(String identifier, String entityIdentifier, String firstName, String lastName, String username, String email, String password, String sshPublicKey) {
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
        this.entityIdentifier = entityIdentifier;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.name = this.firstName + (isNotBlank(this.firstName) ? " " : "") + this.lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sshPublicKey = sshPublicKey;
    }

    public User(String identifier, String entityIdentifier, String name, String username, String email, String password, String sshPublicKey) {
        this(identifier, entityIdentifier, (name.contains(" ") ? name.substring(0,name.lastIndexOf(" ")): name), (name.contains(" ") ?name.substring(name.lastIndexOf(" "), name.length()): name), username, email, password, sshPublicKey);
    }

    public User(String identifier, String name, String username, String email, String password, String sshPublicKey) {
        this(identifier, null, name, username, email, password, sshPublicKey);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
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
