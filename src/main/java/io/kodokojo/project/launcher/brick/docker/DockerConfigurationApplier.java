package io.kodokojo.project.launcher.brick.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.command.PullImageResultCallback;
import io.kodokojo.commons.project.model.BrickConfiguration;
import io.kodokojo.commons.project.model.BrickEntity;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.project.launcher.ConfigurationApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

public class DockerConfigurationApplier implements ConfigurationApplier<BrickConfiguration, BrickEntity> {

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
    public BrickEntity apply(BrickConfiguration brickConfiguration) {
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        BrickDockerCommandFactory brickDockerCommandFactory = commandFactory.get(brickConfiguration.getName());

        ContainerCommand containerCmd = brickDockerCommandFactory.createContainerCmd(dockerClient);
        try {
            dockerClient.pullImageCmd(containerCmd.getImageName().getDockerImageName()).exec(new PullImageResultCallback()).awaitCompletion();
            CreateContainerResponse createContainerResponse = containerCmd.getCreateContainerCmd().exec();
            dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
            BrickEntity brickEntity = new BrickEntity(brickConfiguration.getBrick(), null, 1);
            dockerSupport.waitUntilHttpRequestRespond(containerCmd.createHealthUrl(dockerSupport, createContainerResponse.getId()), containerCmd.getStartTimeout());
            return brickEntity;
        } catch (InterruptedException e) {
            String message = "Unable to pull image " + containerCmd.getImageName().getFullyQualifiedName();
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
