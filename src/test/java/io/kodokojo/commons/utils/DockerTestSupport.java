package io.kodokojo.commons.utils;

/*
 * #%L
 * docker-commons-tests
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
import com.github.dockerjava.api.NotModifiedException;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.apache.commons.lang.StringUtils;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerTestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerTestSupport.class);

    private DockerClient dockerClient;

    private List<String> containerToClean;

    private String remoteDaemonDockerIp;

    private final boolean dockerIsPresent;

    public DockerTestSupport(DockerClientConfig config) {
        dockerClient = DockerClientBuilder.getInstance(config).build();
        String fromEnv = System.getenv("DOCKER_HOST_IP");
        String fromDockerConfig = "127.0.0.1";
        if (config.getUri() != null && config.getUri().getScheme().equals("https")) {
            fromDockerConfig = config.getUri().getHost();
        }
        remoteDaemonDockerIp = StringUtils.isNotBlank(fromEnv) ? fromEnv : fromDockerConfig;
        containerToClean = new ArrayList<>();
        dockerIsPresent = isDockerWorking();
    }

    public DockerTestSupport() {
        this(createDockerConfig());
    }

    public boolean isDockerIsPresent() {
        return dockerIsPresent;
    }

    private static DockerClientConfig createDockerConfig() {
        String dockerHostEnv = System.getenv("DOCKER_HOST");

        String uri = StringUtils.isNotBlank(dockerHostEnv) ? dockerHostEnv.replaceAll("tcp://", "https://") : "unix:///var/run/docker.sock";
        DockerClientConfig.DockerClientConfigBuilder dockerClientConfigBuilder = DockerClientConfig.createDefaultConfigBuilder().withUri(uri);
        String certPath = System.getenv("DOCKER_CERT_PATH");
        if (StringUtils.isNotBlank(certPath)) {
            dockerClientConfigBuilder.withDockerCertPath(certPath);
        }
        return dockerClientConfigBuilder.build();
    }

    public void addContainerIdToClean(String id) {
        containerToClean.add(id);
    }

    public void pullImage(String image) {
        if (isBlank(image)) {
            throw new IllegalArgumentException("image must be defined.");
        }
        if (dockerClient == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().onComplete();
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to pull java image", e);
        }
    }


    public String getContainerName(String containerId) {
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
        return inspectContainerResponse.getName();
    }

    public int getExposedPort(String containerId, int containerPort) {
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();
        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(containerPort));
        if (bindingsExposed == null) {
            return -1;
        }
        return bindingsExposed[0].getHostPort();
    }

    public String getHttpContainerUrl(String containerId, int containerPort) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://").append(getServerIp()).append(":").append(getExposedPort(containerId, containerPort));
        return sb.toString();
    }

    public void stopAndRemoveContainer() {
        boolean remove = !System.getProperty("docker.kill.container", "true").equals("false");
        if (remove) {
            containerToClean.forEach(id -> {
                if (remove) {
                    InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd(id).exec();
                    try {
                        dockerClient.stopContainerCmd(id).exec();
                        //dockerClient.killContainerCmd(id).exec();
                        dockerClient.removeContainerCmd(id).exec();

                        LOGGER.debug("Stopped and removed container id: {}", id);
                    } catch (NotModifiedException e) {
                        LOGGER.error(e.getMessage(),e);
                    }
                } else {
                    LOGGER.warn("You ask us to not stop and remove containers. Ignore container id {}", id);
                }
            });
            containerToClean.clear();
        }
    }

    public void reset() {
        containerToClean.clear();
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public boolean isDockerWorking() {
        return !isNotWorking(dockerClient);
    }

    private boolean isNotWorking(DockerClient dockerClient) {
        if (dockerClient == null) {
            return true;
        }
        try {
            Version version = dockerClient.versionCmd().exec();

            return version == null || StringUtils.isBlank(version.getGitCommit());
        } catch (Exception e) {
            return true;
        }
    }

    public String getServerIp() {
        return remoteDaemonDockerIp;
    }

}
