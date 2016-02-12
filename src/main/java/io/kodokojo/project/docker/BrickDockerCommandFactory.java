package io.kodokojo.project.docker;

import com.github.dockerjava.api.DockerClient;

public interface BrickDockerCommandFactory {

    ContainerCommand createContainerCmd(DockerClient dockerClient);

}
