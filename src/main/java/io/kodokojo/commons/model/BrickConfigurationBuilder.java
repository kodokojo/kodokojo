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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BrickConfigurationBuilder {

    private  String name;

    private  BrickType type;

    private  String version;

    private  Set<PortDefinition> portDefinitions;

    private  Set<BrickConfiguration> dependencies;

    private  Map<String, Serializable> properties;

    public BrickConfigurationBuilder() {
        this(null);
    }

    public BrickConfigurationBuilder(BrickConfiguration brickConfiguration) {

        portDefinitions = new HashSet<>();
        dependencies = new HashSet<>();
        properties = new HashMap<>();
        if (brickConfiguration != null) {
            name = brickConfiguration.getName();
            type = brickConfiguration.getType();
            version = brickConfiguration.getVersion();
            portDefinitions.addAll(brickConfiguration.getPortDefinitions());
            dependencies.addAll(brickConfiguration.getDependencies());
            properties.putAll(brickConfiguration.getProperties());
        }
    }

    public BrickConfiguration build() {
        return new BrickConfiguration(name, type, version, portDefinitions, dependencies, properties);
    }

    public BrickConfigurationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public BrickConfigurationBuilder setType(BrickType type) {
        this.type = type;
        return this;
    }

    public BrickConfigurationBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public BrickConfigurationBuilder setPortDefinitions(Set<PortDefinition> portDefinitions) {
        this.portDefinitions = portDefinitions;
        return this;
    }

    public BrickConfigurationBuilder setDependencies(Set<BrickConfiguration> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public BrickConfigurationBuilder setProperties(Map<String, Serializable> properties) {
        this.properties.putAll(properties);
        return this;
    }
}
