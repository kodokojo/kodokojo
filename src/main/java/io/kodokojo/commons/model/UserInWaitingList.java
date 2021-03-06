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

import java.io.Serializable;

public class UserInWaitingList implements Serializable {

    private final String username;

    private final String email;

    private final long waitingSince;

    public UserInWaitingList(String username, String email, long waitingSince) {
        this.username = username;
        this.email = email;
        this.waitingSince = waitingSince;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getWaitingSince() {
        return waitingSince;
    }

    @Override
    public String toString() {
        return "UserInWaitingList{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", waitingSince=" + waitingSince +
                '}';
    }
}
