package io.kodokojo.docker;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class BrickDockerCommandFactory {

    public abstract ContainerCommand createContainerCmd(DockerClient dockerClient);

    protected static ExposedPort[] exposeTcpPort(int port, Ports ports, ExposedPort[] exposedPorts) {
        if (ports == null) {
            throw new IllegalArgumentException("ports must be defined.");
        }
        if (exposedPorts == null) {
            throw new IllegalArgumentException("exposedPorts must be defined.");
        }
        ExposedPort exposedPort = ExposedPort.tcp(port);
        ports.bind(exposedPort, Ports.Binding(null));
        ArrayList<ExposedPort> list = new ArrayList<>(Arrays.asList(exposedPorts));
        list.add(exposedPort);
        return list.toArray(new ExposedPort[list.size()]);
    }

    protected static ExposedPort exposeSingleTcpPort(int port, Ports ports) {
        if (ports == null) {
            throw new IllegalArgumentException("ports must be defined.");
        }
        ExposedPort exposedPort = ExposedPort.tcp(port);
        ports.bind(exposedPort, Ports.Binding(null));
        return  exposedPort;
    }

}
