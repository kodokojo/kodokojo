package io.kodokojo.project.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.ExposedPorts;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import io.kodokojo.commons.utils.docker.DockerSupport;

import java.util.concurrent.TimeUnit;

public class JenkinsCommandFactory implements BrickDockerCommandFactory {

    @Override
    public ContainerCommand createContainerCmd(DockerClient dockerClient) {
        ExposedPort exposedPort = ExposedPort.tcp(8080);
        Ports portBinding = new Ports();
        portBinding.bind(exposedPort, Ports.Binding(null));
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("jenkins").withExposedPorts(exposedPort).withPortBindings(portBinding);
        return new ContainerCommand(StringToImageNameConverter.convert("jenkins:latest"), createContainerCmd, new ExposedPorts(ExposedPort.tcp(8080)), TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES)) {
            @Override
            public String createHealthUrl(DockerSupport dockerSupport, String id) {
                int exposedPort = dockerSupport.getExposedPort(id, 8080);
                return "http://" + dockerSupport.getDockerHost() + ":" + exposedPort + "/";
            }
        };
    }
}
