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
package io.kodokojo.commons.dto;

import io.kodokojo.commons.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserDto implements Serializable {

    private String identifier;

    private Set<String> entityIdentifiers;

    private String firstName;

    private String lastName;

    private String name;

    private String username;

    private String email;

    private String sshPublicKey;

    private List<UserOrganisationRightDto> organisations;

    public UserDto() {
        super();
    }

    public UserDto(User user) {
        this.identifier = user.getIdentifier();
        this.entityIdentifiers = user.getOrganisationIds();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.sshPublicKey = user.getSshPublicKey();
        this.organisations = new ArrayList<>();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Set<String> getEntityIdentifiers() {
        return entityIdentifiers;
    }

    public void setEntityIdentifiers(Set<String> entityIdentifiers) {
        this.entityIdentifiers = entityIdentifiers;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UserOrganisationRightDto> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<UserOrganisationRightDto> organisations) {
        this.organisations = organisations;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "identifier='" + identifier + '\'' +
                ", entityIdentifiers='" + entityIdentifiers + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", organisations=" + organisations +
                '}';
    }
}
