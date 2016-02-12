package io.kodokojo.project.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.project.BrickLauncher;
import io.kodokojo.project.model.Brick;
import io.kodokojo.project.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerBrickLauncher implements BrickLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerBrickLauncher.class);

    private final DockerSupport dockerSupport;

    private final DockerClient dockerClient;

    private final Map<String, BrickDockerCommandFactory> commandFactory;


    @Inject
    public DockerBrickLauncher(DockerSupport dockerSupport, Map<String, BrickDockerCommandFactory> commandFactory) {
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
    public Service launch(Brick brick) {
        if (brick == null) {
            throw new IllegalArgumentException("brick must be defined.");
        }
        BrickDockerCommandFactory brickDockerCommandFactory = commandFactory.get(brick.getName());

        ContainerCommand containerCmd = brickDockerCommandFactory.createContainerCmd(dockerClient);
        try {
            dockerClient.pullImageCmd(containerCmd.getImageName().getDockerImageName()).exec(new PullImageResultCallback()).awaitCompletion();
            CreateContainerResponse createContainerResponse = containerCmd.getCreateContainerCmd().exec();
            dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
            Service service = new Service(brick, containerCmd.createHealthUrl(dockerSupport, createContainerResponse.getId()));
            dockerSupport.waitUntilHttpRequestRespond(containerCmd.createHealthUrl(dockerSupport, createContainerResponse.getId()), containerCmd.getStartTimeout());
            return service;
        } catch (InterruptedException e) {
            String message = "Unable to pull image " + containerCmd.getImageName().getFullyQualifiedName();
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

}
