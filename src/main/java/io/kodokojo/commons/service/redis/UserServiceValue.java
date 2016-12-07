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



import io.kodokojo.commons.model.UserService;

import java.io.Serializable;

public class UserServiceValue implements Serializable {

    private String name;

    private String login;

    private byte[] password;

    private byte[] privateKey;

    private byte[] publicKey;

    public UserServiceValue(String name, String login, byte[] password, byte[] privateKey, byte[] publicKey) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public UserServiceValue(UserService userService, byte[] password, byte[] privateKey, byte[] publicKey) {
        this(userService.getName(), userService.getLogin(), password, privateKey, publicKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getLogin() {
        return login;
    }

    public byte[] getPassword() {
        return password;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "UserServiceValue{" +
                "name='" + name + '\'' +
                ", login='" + login + '\'' +
                '}';
    }
}
