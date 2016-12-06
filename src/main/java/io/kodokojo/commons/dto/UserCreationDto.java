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
package io.kodokojo.commons.dto;



import io.kodokojo.commons.model.User;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserCreationDto extends User {

    private final String privateKey;

    public UserCreationDto(String identifier,String entityIdentifier, String name, String username, String email, String password, String privateKey, String sshPublicKey) {
        super(identifier,entityIdentifier,  name, username, email, password, sshPublicKey);
        if (isBlank(privateKey)) {
            throw new IllegalArgumentException("privateKey must be defined.");
        }
        this.privateKey = privateKey;
    }

    public UserCreationDto(User user, String privateKey) {
        this(user.getIdentifier(), user.getEntityIdentifier(), user.getName(), user.getUsername(), user.getEmail(), user.getPassword(), privateKey, user.getSshPublicKey());
    }
    public String getPrivateKey() {
        return privateKey;
    }
}
