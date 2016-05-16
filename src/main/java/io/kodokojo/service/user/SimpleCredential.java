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
package io.kodokojo.service.user;



import static org.apache.commons.lang.StringUtils.isBlank;

public class SimpleCredential implements Credential {

    private final String username;

    private final String password;

    public SimpleCredential(String username, String password) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException(" must be defined.");
        }
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String identity() {
        return username;
    }
}
