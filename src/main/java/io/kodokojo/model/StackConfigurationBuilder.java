package io.kodokojo.model;

import java.util.HashSet;
import java.util.Set;

public class StackConfigurationBuilder {

    private String name;

    private StackType type;

    private Set<BrickConfiguration> brickConfigurations;

    private String loadBalancerHost;

    private int scmSshPort;

    public StackConfigurationBuilder(StackConfiguration init) {
        if (init == null) {
            throw new IllegalArgumentException("init must be defined.");
        }
        this.name = init.getName();
        this.type = init.getType();
        this.brickConfigurations = new HashSet<>();
        this.brickConfigurations.addAll(init.getBrickConfigurations());
        this.loadBalancerHost = init.getLoadBalancerHost();
        this.scmSshPort = init.getScmSshPort();
    }

    public StackConfiguration build() {
        return new StackConfiguration(name, type, brickConfigurations, loadBalancerHost, scmSshPort);
    }

    public StackConfigurationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public StackConfigurationBuilder setType(StackType type) {
        this.type = type;
        return this;
    }

    public StackConfigurationBuilder setBrickConfigurations(Set<BrickConfiguration> brickConfigurations) {
        this.brickConfigurations = brickConfigurations;
        return this;
    }

    public StackConfigurationBuilder addBrickConfiguration(BrickConfiguration brickConfiguration) {
        this.brickConfigurations.add(brickConfiguration);
        return this;
    }

    public StackConfigurationBuilder setLoadBalancerHost(String loadBalancerHost) {
        this.loadBalancerHost = loadBalancerHost;
        return this;
    }

    public StackConfigurationBuilder setScmSshPort(int scmSshPort) {
        this.scmSshPort = scmSshPort;
        return this;
    }
}
