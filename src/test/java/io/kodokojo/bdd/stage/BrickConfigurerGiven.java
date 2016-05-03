package io.kodokojo.bdd.stage;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
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
    public DockerTestSupport dockerTestSupport = new DockerTestSupport();

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

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickConfigurerGiven.class);

    public SELF $_is_started(@Quoted String brickName,@Hidden String image, @Hidden int port, @Hidden int timeout, @Hidden UserAuthenticator userAuthenticator) {
        DockerClient dockerClient = dockerTestSupport.getDockerClient();
        LOGGER.info("Pulling docker image {}", image);
        dockerTestSupport.pullImage(image);

        this.brickName = brickName.toLowerCase();
        this.userAuthenticator = userAuthenticator;
        assertThat(image).isNotNull();
        LOGGER.info("Starting Docker image {} to run brick {}.", image, brickName);

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(port);
        portBinding.bind(exposedPort, Ports.Binding(null));

        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(image)
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .exec();
        dockerTestSupport.addContainerIdToClean(containerResponse.getId());
        dockerClient.startContainerCmd(containerResponse.getId()).exec();

        brickUrl = dockerTestSupport.getHttpContainerUrl(containerResponse.getId(),port);

        boolean brickStarted = waitBrickStarted(brickUrl, timeout);
        assertThat(brickStarted).isTrue();
        LOGGER.info("Brick {} successfully started.", brickName);
        brickFactory = new DefaultBrickFactory(null);
        brickConfigurerProvider = new DefaultBrickConfigurerProvider(new DefaultBrickUrlFactory("kodokojo.dev"));
        return self();
    }

    private boolean waitBrickStarted(String brickUrl, int timeout) {
        boolean started = false;
        long now = System.currentTimeMillis();
        long end = now + (timeout*1000);

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(brickUrl).build();
        while(!started && (end - System.currentTimeMillis()) > 0) {
            Response response = null;
            try {
                response = httpClient.newCall(request).execute();
                int httpStatusCode = response.code();
                started = httpStatusCode >= 200 && httpStatusCode < 300;
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

    @AfterScenario
    public void tearDown() {
        dockerTestSupport.stopAndRemoveContainer();
    }
}
