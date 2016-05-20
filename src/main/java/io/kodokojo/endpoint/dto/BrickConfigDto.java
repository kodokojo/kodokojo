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
package io.kodokojo.endpoint.dto;

import io.kodokojo.model.BrickConfiguration;

import java.io.Serializable;

public class BrickConfigDto implements Serializable {

    private String name;

    private String type;

    public BrickConfigDto(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public BrickConfigDto(BrickConfiguration brickConfiguration) {
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        this.name = brickConfiguration.getName();
        this.type = brickConfiguration.getType().name();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BrickConfigDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
