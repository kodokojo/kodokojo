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
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.ExposedPorts;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import io.kodokojo.commons.utils.docker.DockerSupport;

import java.util.concurrent.TimeUnit;

public class GitlabCommandFactory extends BrickDockerCommandFactory {

    @Override
    public ContainerCommand createContainerCmd(DockerClient dockerClient) {
        ExposedPort[] exposedPorts =  new ExposedPort[0];
        Ports portBinding = new Ports();

        exposeTcpPort(80, portBinding, exposedPorts);
        exposeTcpPort(443, portBinding, exposedPorts);
        exposeTcpPort(22, portBinding, exposedPorts);

        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("gitlab/gitlab-ce").withExposedPorts(exposedPorts).withPortBindings(portBinding);
        return new ContainerCommand(StringToImageNameConverter.convert("gitlab/gitlab-ce"), createContainerCmd, new ExposedPorts(ExposedPort.tcp(80), ExposedPort.tcp(443), ExposedPort.tcp(22)), TimeUnit.MILLISECONDS.convert(5,TimeUnit.MINUTES)) {
            @Override
            public String createHealthUrl(DockerSupport dockerSupport, String id) {
                int exposedPort = dockerSupport.getExposedPort(id, 80);
                return "http://" + dockerSupport.getDockerHost() + ":" + exposedPort + "/";
            }
        };
    }
}
