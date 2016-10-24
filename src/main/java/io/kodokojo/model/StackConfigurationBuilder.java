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
package io.kodokojo.model;

import java.util.HashSet;
import java.util.Set;

public class StackConfigurationBuilder {

    private String name;

    private StackType type;

    private Set<BrickConfiguration> brickConfigurations;

    private String loadBalancerHost;

    private int scmSshPort;

    public StackConfigurationBuilder() {
        this(null);
    }

    public StackConfigurationBuilder(StackConfiguration init) {
        if (init == null) {
            brickConfigurations = new HashSet<>();
        } else {
            this.name = init.getName();
            this.type = init.getType();
            this.brickConfigurations = new HashSet<>();
            this.brickConfigurations.addAll(init.getBrickConfigurations());
            this.scmSshPort = init.getScmSshPort();
        }
    }

    public StackConfiguration build() {
        return new StackConfiguration(name, type, brickConfigurations, scmSshPort);
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

    public StackConfigurationBuilder setScmSshPort(int scmSshPort) {
        this.scmSshPort = scmSshPort;
        return this;
    }
}
