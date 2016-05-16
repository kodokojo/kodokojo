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
package io.kodokojo.entrypoint.dto;

import java.io.Serializable;
import java.util.List;

public class ProjectCreationDto implements Serializable {

    private String name;

    private String entityIdentifier;

    private String ownerIdentifier;

    private List<String> userIdentifiers;

    public ProjectCreationDto(String entityIdentifier, String name, String ownerIdentifier, List<String> userIdentifiers) {
        this.entityIdentifier = entityIdentifier;
        this.name = name;
        this.ownerIdentifier = ownerIdentifier;
        this.userIdentifiers = userIdentifiers;
    }

    public String getOwnerIdentifier() {
        return ownerIdentifier;
    }

    public void setOwnerIdentifier(String ownerIdentifier) {
        this.ownerIdentifier = ownerIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUserIdentifiers() {
        return userIdentifiers;
    }

    public void setUserIdentifiers(List<String> userIdentifiers) {
        this.userIdentifiers = userIdentifiers;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    @Override
    public String toString() {
        return "ProjectCreationDto{" +
                "name='" + name + '\'' +
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", ownerIdentifier='" + ownerIdentifier + '\'' +
                ", userIdentifiers=" + userIdentifiers +
                '}';
    }
}
