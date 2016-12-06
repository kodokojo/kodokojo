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
package io.kodokojo.commons.model;


import io.kodokojo.commons.RSAUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserService {

    private final String identifier;

    private final String name;

    private final String login;

    private final String password;

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    public UserService(String identifier, String name, String login, String password, RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(login)) {
            throw new IllegalArgumentException("login must be defined.");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("password must be defined.");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey must be defined.");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey must be defined.");
        }
        this.identifier = identifier;
        this.name = name;
        this.login = login;
        this.password = password;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        String encodePublicKey = RSAUtils.encodePublicKey(publicKey, name);

        return "UserService{" +
                "name='" + name + '\'' +
                "identifier='" + identifier + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", publicKeys=" + encodePublicKey +
                '}';
    }
}
