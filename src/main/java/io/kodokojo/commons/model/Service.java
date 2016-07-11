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

import static org.apache.commons.lang.StringUtils.isBlank;

public class Service implements Serializable {

    private final String name;

    private final String host;

    private final int port;

    private final ServiceType type;

    public Service(String name, String host, int port, ServiceType type) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be upper than 0");
        }
        this.name = name;
        this.host = host;
        this.port = port;
        this.type = type;
    }

    public Service(String name, String host, int port) {
        this(name, host, port, ServiceType.UNKNOWN);
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ServiceType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", type=" + type +
                '}';
    }
}
