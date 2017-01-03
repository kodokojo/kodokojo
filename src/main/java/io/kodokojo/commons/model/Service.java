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
package io.kodokojo.commons.model;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Service implements Serializable {

    private final String name;

    private final String host;

    private final PortDefinition portDefinition;

    public Service(String name, String host, PortDefinition portDefinition) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        requireNonNull(portDefinition, "portDefinition must be defined.");
        this.name = name;
        this.host = host;
        this.portDefinition = portDefinition;
    }


    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public PortDefinition getPortDefinition() {
        return portDefinition;
    }

    public int getPort() {
        return portDefinition.getContainerPort();
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", portDefinition=" + portDefinition +
                '}';
    }
}
