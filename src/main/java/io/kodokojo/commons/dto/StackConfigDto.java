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

import io.kodokojo.commons.model.StackConfiguration;

import java.util.ArrayList;
import java.util.List;

public class StackConfigDto {

    private String name;

    private String type;

    private List<BrickConfigDto> brickConfigs;

    public StackConfigDto(String name, String type, List<BrickConfigDto> brickConfigs) {
        this.name = name;
        this.type = type;
        this.brickConfigs = brickConfigs;
    }

    public StackConfigDto(StackConfiguration stackConfiguration) {
        if (stackConfiguration == null) {
            throw new IllegalArgumentException("stackConfiguration must be defined.");
        }
        this.name = stackConfiguration.getName();
        this.type = stackConfiguration.getType().name();
        this.brickConfigs = new ArrayList<>(stackConfiguration.getBrickConfigurations().size());
        stackConfiguration.getBrickConfigurations().forEach(brickConfiguration -> brickConfigs.add(new BrickConfigDto(brickConfiguration, brickConfiguration.getVersion())));
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

    public List<BrickConfigDto> getBrickConfigs() {
        return brickConfigs;
    }

    public void setBrickConfigs(List<BrickConfigDto> brickConfigs) {
        this.brickConfigs = brickConfigs;
    }

    @Override
    public String toString() {
        return "StackConfigDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", brickConfigs=" + brickConfigs +
                '}';
    }
}
