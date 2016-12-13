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


import com.google.gson.annotations.Expose;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickConfiguration implements Serializable {

    @Expose
    private final String name;

    @Expose
    private final BrickType type;

    @Expose
    private final String version;

    @Expose
    private final Set<PortDefinition> portDefinitions;

    @Expose
    private final Set<BrickConfiguration> dependencies;

    @Expose
    private final Map<String, Serializable> properties;

    public BrickConfiguration(String name, BrickType type, String version, Set<PortDefinition> portDefinitions, Set<BrickConfiguration> dependencies, Map<String, Serializable> properties) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (isBlank(version)) {
            throw new IllegalArgumentException("version must be defined.");
        }
        if (CollectionUtils.isEmpty(portDefinitions)) {
            throw new IllegalArgumentException("portDefinitions must be defined.");
        }
        if (dependencies == null) {
            throw new IllegalArgumentException("dependencies must be defined.");
        }
        this.name = name;
        this.type = type;
        this.version = version;
        this.portDefinitions = portDefinitions;
        this.dependencies = dependencies;
        this.properties = properties;
    }

    public BrickConfiguration(String name, BrickType type, String version, Set<PortDefinition> portDefinitions, Map<String, Serializable> properties) {
        this(name,type,version,portDefinitions, new HashSet<>(), properties);
    }

    public BrickConfiguration(String name, BrickType type, String version, Set<PortDefinition> portDefinitions) {
        this(name, type, version, portDefinitions, new HashSet<>(), new HashedMap<>());
    }

    public String getName() {
        return name;
    }

    public BrickType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public Set<PortDefinition> getPortDefinitions() {
        return new HashSet<>(portDefinitions);
    }

    public Set<BrickConfiguration> getDependencies() {
        return new HashSet<>(dependencies);
    }

    public Map<String, Serializable> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "BrickConfiguration{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", version='" + version + '\'' +
                ", portDefinitions=" + portDefinitions +
                ", dependencies=" + dependencies +
                ", properties=" + properties +
                '}';
    }
}
