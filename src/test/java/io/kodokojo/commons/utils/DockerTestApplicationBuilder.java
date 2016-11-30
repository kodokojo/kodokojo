package io.kodokojo.commons.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.PullImageResultCallback;
import io.kodokojo.commons.model.PortDefinition;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

public interface DockerTestApplicationBuilder {

    Logger LOGGER = LoggerFactory.getLogger(DockerTestApplicationBuilder.class);

    String TAG_LATEST = "latest";

    default Try<DockerService> startRedis(DockerTestSupport dockerTestSupport) {
        requireNonNull(dockerTestSupport, "dockerTestSupport must be defined.");

        ServiceChecker serviceChecker = (dockerTestSupport1, createContainerResponse) -> {
            String redisHost = dockerTestSupport1.getContainerPublicIp(createContainerResponse.getId());
            int redisPort = dockerTestSupport1.getExposedPort(createContainerResponse.getId(), 6379);

            long end = System.currentTimeMillis() + 60000;
            boolean redisIsReady = false;
            long nbTry = 0;
            while (!redisIsReady && (end - System.currentTimeMillis()) > 0) {
                JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
                try (Jedis jedis = jedisPool.getResource()) {
                    String resPing = jedis.ping();
                    redisIsReady = "PONG".equals(resPing);
                } catch (JedisConnectionException e) {
                    //  Silently ignore, Redis may not be available
                    if (nbTry++ % 100 == 0) {
                        LOGGER.debug("Unable to connect to redis {}:{}", redisHost, redisPort);
                    }
                }
                if (!redisIsReady) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            assertThat(redisIsReady).isTrue();
        };
        return startDockerService(dockerTestSupport, "redis", "redis", TAG_LATEST, 6379, serviceChecker);
    }

    default Try<DockerService> startRabbitMq(DockerTestSupport dockerTestSupport) {
        requireNonNull(dockerTestSupport, "dockerTestSupport must be defined.");

        ServiceChecker serviceChecker = new HttpServiceChecker(15672);

        return startDockerService(dockerTestSupport, "rabbitmq", "rabbitmq", "3-management", new int[]{5672, 15672}, serviceChecker);
    }

    default Try<DockerService> startDockerService(DockerTestSupport dockerTestSupport, String serviceName, String imageName, String tag, int containerPort, ServiceChecker serviceChecker) {
        return startDockerService(dockerTestSupport, serviceName, imageName, tag, new int[]{containerPort}, serviceChecker);
    }

    default Try<DockerService> startDockerService(DockerTestSupport dockerTestSupport, String serviceName, String imageName, String tag, int[] containerPorts, ServiceChecker serviceChecker) {
        requireNonNull(dockerTestSupport, "dockerTestSupport must be defined.");
        if (isBlank(serviceName)) {
            throw new IllegalArgumentException("serviceName must be defined.");
        }
        if (isBlank(imageName)) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (isBlank(tag)) {
            tag = TAG_LATEST;
        }
        requireNonNull(containerPorts, "containerPorts must be defined.");
        if (containerPorts.length < 1) {
            throw new IllegalArgumentException("containerPorts must contain almost one value.");
        }
        requireNonNull(serviceChecker, "serviceChecker must be defined.");

        checkIfDockerImageIsPulled(dockerTestSupport, imageName, tag);

        DockerClient dockerClient = dockerTestSupport.getDockerClient();

        Ports portBinding = new Ports();
        List<ExposedPort> exposedPorts = new ArrayList<>();
        for (int containerPort : containerPorts) {
            ExposedPort exposedPort = ExposedPort.tcp(containerPort);
            portBinding.bind(exposedPort, new Ports.Binding(null, null));
            exposedPorts.add(exposedPort);
        }

        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(imageName + ":" + tag)
                .withExposedPorts(exposedPorts)
                .withPortBindings(portBinding)
                .exec();
        dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        dockerTestSupport.addContainerIdToClean(createContainerResponse.getId());

        serviceChecker.checkServiceIsRunning(dockerTestSupport, createContainerResponse);

        String host = dockerTestSupport.getContainerPublicIp(createContainerResponse.getId());
        int port = dockerTestSupport.getExposedPort(createContainerResponse.getId(), containerPorts[0]);
        LOGGER.info("Service {} running on {}:{}", serviceName, host, port);

        return Try.success(new DockerService(createContainerResponse.getId(), serviceName, host, new PortDefinition(port)));

    }

    default void checkIfDockerImageIsPulled(DockerTestSupport dockerTestSupport, String imageName) {
        checkIfDockerImageIsPulled(dockerTestSupport, imageName, null);
    }

    default void checkIfDockerImageIsPulled(DockerTestSupport dockerTestSupport, String imageName, String tag) {
        requireNonNull(dockerTestSupport, "dockerTestSupport must be defined.");
        if (isBlank(imageName)) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (isBlank(tag)) {
            tag = TAG_LATEST;
        }

        DockerClient dockerClient = dockerTestSupport.getDockerClient();
        String lookupImageName = imageName + ":" + tag;

        List<Image> images = dockerClient.listImagesCmd().exec();

        boolean foundRedis = images.stream()
                .filter(i -> i != null && i.getRepoTags() != null)
                .filter(i -> Arrays.asList(i.getRepoTags()).contains(lookupImageName))
                .count() >= 1;

        //if (!foundRedis || tag.equals(TAG_LATEST)) {
        if (!foundRedis) {
            LOGGER.info("Pulling docker image {}:{}.", imageName, tag);
            dockerClient.pullImageCmd(imageName + ":" + tag).exec(new PullImageResultCallback()).awaitSuccess();
            LOGGER.info("Docker image {}:{} pulled.", imageName, tag);
        } else {
            LOGGER.info("Docker image {}:{} which have a not tagged 'latest' already pulled.", imageName, tag);
        }
    }


    interface ServiceChecker {
        void checkServiceIsRunning(DockerTestSupport dockerTestSupport, CreateContainerResponse createContainerResponse);
    }

}
