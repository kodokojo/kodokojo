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


import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class StackConfiguration implements Serializable {

    private final String name;

    private final StackType type;

    private final Set<BrickConfiguration> brickConfigurations;

    private final int scmSshPort;

    public StackConfiguration(String name, StackType type, Set<BrickConfiguration> brickConfigurations, int scmSshPort) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (CollectionUtils.isEmpty(brickConfigurations)) {
            throw new IllegalArgumentException("brickConfigurations must be defined.");
        }

        this.name = name;
        this.type = type;
        this.brickConfigurations = brickConfigurations;
        this.scmSshPort = scmSshPort;
    }

    public String getName() {
        return name;
    }

    public StackType getType() {
        return type;
    }

    public Set<BrickConfiguration> getBrickConfigurations() {
        return new HashSet<>(brickConfigurations);
    }

    public int getScmSshPort() {
        return scmSshPort;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackConfiguration stackConfiguration = (StackConfiguration) o;

        if (!name.equals(stackConfiguration.name)) return false;
        if (type != stackConfiguration.type) return false;
        return brickConfigurations.equals(stackConfiguration.brickConfigurations);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + brickConfigurations.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "StackConfiguration{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", brickConfigurations=" + brickConfigurations +
                ", scmSshPort=" + scmSshPort +
                '}';
    }
}
