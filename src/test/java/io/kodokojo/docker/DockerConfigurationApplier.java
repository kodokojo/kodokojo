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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.ExposedPorts;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.BrickDeploymentState;
import io.kodokojo.project.starter.ConfigurationApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DockerConfigurationApplier implements ConfigurationApplier<BrickConfiguration, BrickDeploymentState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerConfigurationApplier.class);

    private final DockerSupport dockerSupport;

    private final DockerClient dockerClient;

    private final Map<String, BrickDockerCommandFactory> commandFactory;


    @Inject
    public DockerConfigurationApplier(DockerSupport dockerSupport, Map<String, BrickDockerCommandFactory> commandFactory) {
        if (dockerSupport == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        if (commandFactory == null) {
            throw new IllegalArgumentException("commandFactory must be defined.");
        }
        this.dockerSupport = dockerSupport;
        this.commandFactory = commandFactory;
        this.dockerClient = dockerSupport.createDockerClient();
    }

    @Override
    public BrickDeploymentState apply(BrickConfiguration brickConfiguration) {
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        BrickDockerCommandFactory brickDockerCommandFactory = commandFactory.get(brickConfiguration.getName());

        ContainerCommand containerCmd = brickDockerCommandFactory.createContainerCmd(dockerClient);
        try {
            //dockerClient.pullImageCmd(containerCmd.getImageName().getDockerImageName()).exec(new PullImageResultCallback()).awaitCompletion();
            CreateContainerResponse createContainerResponse = containerCmd.getCreateContainerCmd().exec();
            dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
            List<Service> services = new ArrayList<>();
            ExposedPorts ports = containerCmd.getExposedPorts();
            String host = dockerSupport.getDockerHost();
            for (ExposedPort exposedPort : ports.getExposedPorts()) {
                int port = dockerSupport.getExposedPort(createContainerResponse.getId(), exposedPort.getPort());
                services.add(new Service(brickConfiguration.getName() + "-" + port, host, port));
            }
            BrickDeploymentState brickDeploymentState = new BrickDeploymentState(brickConfiguration.getBrick(), services, 1);
            dockerSupport.waitUntilHttpRequestRespond(containerCmd.createHealthUrl(dockerSupport, createContainerResponse.getId()), containerCmd.getStartTimeout());
            return brickDeploymentState;
        } catch (Exception e) {
            return null;
        }
        /*
        catch (InterruptedException e) {
            String message = "Unable to pull image " + containerCmd.getImageName().getFullyQualifiedName();
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }*/
    }
}
