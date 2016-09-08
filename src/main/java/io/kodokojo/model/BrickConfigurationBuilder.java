package io.kodokojo.model;

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

    public BrickConfigurationBuilder(BrickConfiguration brickConfiguration) {
        if (brickConfiguration != null) {
            name = brickConfiguration.getName();
            type = brickConfiguration.getType();
            version = brickConfiguration.getVersion();
            portDefinitions = new HashSet<>(brickConfiguration.getPortDefinitions());
            dependencies = new HashSet<>(brickConfiguration.getDependencies());
            properties = new HashMap<>(brickConfiguration.getProperties());
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
