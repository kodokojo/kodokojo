/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor.message;

import io.kodokojo.model.User;

import java.io.Serializable;

public class UserRequestMessage implements Serializable {

    protected final User requester;

    public UserRequestMessage(User requester) {
        this.requester = requester;
    }

    public User getRequester() {
        return requester;
    }

    @Override
    public String toString() {
        return "UserRequestMessage[" + getClass().getCanonicalName() + "]{" +
                "requester=" + requester +
                '}';
    }
}
