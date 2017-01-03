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

import io.kodokojo.commons.model.User;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class ProjectConfigurationChangeUserRequest implements Serializable {

    private final TypeChange typeChange;

    private final String projectConfigurationId;

    private final List<String> userIdentifiers;

    private final User requester;

    public ProjectConfigurationChangeUserRequest(User requester, io.kodokojo.commons.event.payload.TypeChange typeChange, String projectConfigurationId, List<String> userIdentifiers) {
        requireNonNull(requester, "requester must be defined.");
        requireNonNull(typeChange, "typeChange must be defined.");
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (userIdentifiers == null) {
            throw new IllegalArgumentException("userIdentifiers must be defined.");
        }
        this.requester = requester;
        this.typeChange = typeChange;
        this.projectConfigurationId = projectConfigurationId;
        this.userIdentifiers = userIdentifiers;
    }

    public TypeChange getTypeChange() {
        return typeChange;
    }

    public String getProjectConfigurationId() {
        return projectConfigurationId;
    }

    public List<String> getUserIdentifiers() {
        return userIdentifiers;
    }

    public User getRequester() {
        return requester;
    }
}
