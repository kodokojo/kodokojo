package io.kodokojo.project.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.ExposedPorts;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import io.kodokojo.commons.utils.docker.DockerSupport;

import java.util.concurrent.TimeUnit;

public class GitlabCommandFactory implements BrickDockerCommandFactory {

    @Override
    public ContainerCommand createContainerCmd(DockerClient dockerClient) {
        ExposedPort exposedPort = ExposedPort.tcp(80);
        Ports portBinding = new Ports();
        portBinding.bind(exposedPort, Ports.Binding(null));
        exposedPort = ExposedPort.tcp(443);
        portBinding.bind(exposedPort, Ports.Binding(null));
        exposedPort = ExposedPort.tcp(22);
        portBinding.bind(exposedPort, Ports.Binding(null));
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("gitlab/gitlab-ce").withExposedPorts(exposedPort).withPortBindings(portBinding);
        return new ContainerCommand(StringToImageNameConverter.convert("gitlab/gitlab-ce"), createContainerCmd, new ExposedPorts(ExposedPort.tcp(80), ExposedPort.tcp(443), ExposedPort.tcp(22)), TimeUnit.MILLISECONDS.convert(5,TimeUnit.MINUTES)) {
            @Override
            public String createHealthUrl(DockerSupport dockerSupport, String id) {
                int exposedPort = dockerSupport.getExposedPort(id, 80);
                return "http://" + dockerSupport.getDockerHost() + ":" + exposedPort + "/";
            }
        };
    }
}
