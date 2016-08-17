/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        if (config.getDockerHost() != null && config.getDockerHost().getScheme().equals("https")) {
            fromDockerConfig = config.getDockerHost().getHost();
        }
        remoteDaemonDockerIp = StringUtils.isNotBlank(fromEnv) ? fromEnv : fromDockerConfig;
        //LOGGER.debug("Defined Ip to access to services to {}", remoteDaemonDockerIp);
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

        String uri = StringUtils.isNotBlank(dockerHostEnv) ? dockerHostEnv : null;
        DefaultDockerClientConfig.Builder defaultConfigBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        if (uri != null) {
            defaultConfigBuilder = defaultConfigBuilder.withDockerHost(uri);
        }
        String certPath = System.getenv("DOCKER_CERT_PATH");
        if (StringUtils.isNotBlank(certPath)) {
            defaultConfigBuilder.withDockerCertPath(certPath);
        }
        DefaultDockerClientConfig res = defaultConfigBuilder.build();
        //LOGGER.debug("User Docker host  {}", res.getDockerHost().toString());
        return res;
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

    public String getContainerPublicIp(String containerId) {
        return remoteDaemonDockerIp;

    }
    public int getExposedPort(String containerId, int containerPort) {
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();
        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(containerPort));

        if (bindingsExposed == null) {
            return -1;
        }
        String hostPortSpec = bindingsExposed[0].getHostPortSpec();
        return Integer.parseInt(hostPortSpec);
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

                    dockerClient.stopContainerCmd(id).exec();
                    //dockerClient.killContainerCmd(id).exec();
                    dockerClient.removeContainerCmd(id).exec();

                    LOGGER.debug("Stopped and removed container id: {}", id);

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
            e.printStackTrace();
            return true;
        }
    }

    public String getServerIp() {
        return remoteDaemonDockerIp;
    }

}
