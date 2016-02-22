package io.kodokojo.project.launcher.brick.docker;

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
