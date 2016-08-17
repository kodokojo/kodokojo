/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.bdd.stage;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.bdd.stage.brickauthenticator.UserAuthenticator;
import io.kodokojo.brick.*;
import io.kodokojo.commons.utils.DockerTestSupport;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class BrickConfigurerGiven<SELF extends BrickConfigurerGiven<?>> extends Stage<SELF> {


    @ProvidedScenarioState
    public DockerTestSupport dockerTestSupport;

    @ProvidedScenarioState
    String containerId;

    @ProvidedScenarioState
    String brickName;

    @ProvidedScenarioState
    String brickUrl;

    @ProvidedScenarioState
    UserAuthenticator userAuthenticator;

    @ProvidedScenarioState
    BrickFactory brickFactory;

    @ProvidedScenarioState
    BrickConfigurerProvider brickConfigurerProvider;

    @ProvidedScenarioState
    HttpUserSupport httpUserSupport;

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickConfigurerGiven.class);

    public SELF $_is_started(@Hidden DockerTestSupport dockerTestSupport, @Quoted String brickName, @Hidden String image, @Hidden int port, @Hidden int timeout, @Hidden UserAuthenticator userAuthenticator) {
        if (this.dockerTestSupport != null) {
            this.dockerTestSupport.stopAndRemoveContainer();
        }
        this.dockerTestSupport = dockerTestSupport;
        DockerClient dockerClient = this.dockerTestSupport.getDockerClient();
        LOGGER.info("Pulling docker image {}", image);
        this.dockerTestSupport.pullImage(image);

        this.brickName = brickName.toLowerCase();
        this.userAuthenticator = userAuthenticator;
        assertThat(image).isNotNull();
        LOGGER.info("Starting Docker image {} to run brick {}.", image, brickName);

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(port);
        portBinding.bind(exposedPort, new Ports.Binding(null, "80"));

        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(image)
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .withHostName(dockerTestSupport.getServerIp())
                .exec();
        containerId = containerResponse.getId();
        this.dockerTestSupport.addContainerIdToClean(containerId);
        dockerClient.startContainerCmd(containerId).exec();

        brickUrl = this.dockerTestSupport.getHttpContainerUrl(containerId, port);

        boolean brickStarted = waitBrickStarted(brickUrl, timeout);
        assertThat(brickStarted).isTrue();
        LOGGER.info("Brick {} successfully started.", brickName);
        brickFactory = new DefaultBrickFactory();
        brickConfigurerProvider = new DefaultBrickConfigurerProvider(new DefaultBrickUrlFactory("kodokojo.dev"), new OkHttpClient());
        return self();
    }

    private boolean waitBrickStarted(String brickUrl, int timeout) {
        boolean started = false;
        long now = System.currentTimeMillis();
        long end = now + (timeout * 1000);

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(brickUrl).build();
        while (!started && (end - System.currentTimeMillis()) > 0) {
            Response response = null;
            try {
                response = httpClient.newCall(request).execute();
                int httpStatusCode = response.code();
                //LOGGER.debug("Wait brick {}.",response.toString());
                started = (httpStatusCode >= 200 && httpStatusCode < 405);
            } catch (IOException e) {
                // Silently ignore, service maybe not available
                started = false;
            } finally {
                if (response != null) {
                    IOUtils.closeQuietly(response.body());
                }
            }
            if (!started) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return started;
    }

}
