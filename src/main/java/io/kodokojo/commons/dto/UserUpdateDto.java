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

public class UserUpdateDto implements Serializable {

    private String identifier;

    private String entityIdentifier;

    private String firstName;

    private String lastName;

    private String password;

    private String username;

    private String email;

    private String sshPublicKey;

    private List<UserProjectConfigIdDto> projectConfigurationIds;

    public UserUpdateDto() {
        super();
    }

    public UserUpdateDto(User user) {
        this.identifier = user.getIdentifier();
        this.entityIdentifier = user.getEntityIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.password = user.getPassword();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.sshPublicKey = user.getSshPublicKey();
        this.projectConfigurationIds = new ArrayList<>();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<UserProjectConfigIdDto> getProjectConfigurationIds() {
        return projectConfigurationIds;
    }

    public void setProjectConfigurationIds(List<UserProjectConfigIdDto> projectConfigurationIds) {
        this.projectConfigurationIds = projectConfigurationIds;
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
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", projectConfigurationIds=" + projectConfigurationIds +
                '}';
    }
}
