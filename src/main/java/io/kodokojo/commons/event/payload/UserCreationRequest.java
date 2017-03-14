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
package io.kodokojo.commons.event.payload;

import java.io.Serializable;

public class UserCreationRequest implements Serializable {

    private final String id;

    private final String email;

    private final String username;

    private final String entityId;

    private final boolean isRoot;

    public UserCreationRequest(String id, String email, String username, String entityId, boolean isRoot) {

        this.id = id;
        this.email = email;
        this.username = username;
        this.entityId = entityId;
        this.isRoot = isRoot;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isRoot() {
        return isRoot;
    }
}
