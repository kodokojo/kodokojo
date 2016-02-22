package io.kodokojo.project.launcher.brick.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.ExposedPorts;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import io.kodokojo.commons.utils.docker.DockerSupport;

import java.util.concurrent.TimeUnit;

public class JenkinsCommandFactory extends BrickDockerCommandFactory {

    @Override
    public ContainerCommand createContainerCmd(DockerClient dockerClient) {

        ExposedPort[] exposedPorts =  new ExposedPort[0];
        Ports portBinding = new Ports();

        exposeTcpPort(8080, portBinding, exposedPorts);
        exposeTcpPort(50000, portBinding, exposedPorts);

        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("jenkins").withExposedPorts(exposedPorts).withPortBindings(portBinding);
        return new ContainerCommand(StringToImageNameConverter.convert("jenkins:latest"), createContainerCmd, new ExposedPorts(ExposedPort.tcp(8080)), TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES)) {
            @Override
            public String createHealthUrl(DockerSupport dockerSupport, String id) {
                int exposedPort = dockerSupport.getExposedPort(id, 8080);
                return "http://" + dockerSupport.getDockerHost() + ":" + exposedPort + "/";
            }
        };
    }
}
