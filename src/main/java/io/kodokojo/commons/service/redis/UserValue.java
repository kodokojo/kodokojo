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
package io.kodokojo.commons.service.redis;



import io.kodokojo.commons.model.User;

import java.io.Serializable;

public class UserValue implements Serializable {

    private String entityId;

    private String name;

    private String username;

    private String email;

    private byte[] password;

    private String sshPublicKey;

    public UserValue(String name, String username,String entityId, String email, byte[] password, String sshPublicKey) {
        this.name = name;
        this.entityId = entityId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sshPublicKey = sshPublicKey;
    }

    public UserValue(User user, byte[] password) {
        this(user.getName(), user.getUsername(), user.getEntityIdentifier(), user.getEmail(), password, user.getSshPublicKey());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
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

    public byte[] getPassword() {
        return password;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    @Override
    public String toString() {
        return "UserValue{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", entityId='" + entityId + '\'' +
                ", email='" + email + '\'' +
                ", sshPublicKey='" + sshPublicKey + '\'' +
                '}';
    }
}
