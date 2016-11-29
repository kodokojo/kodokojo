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
package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.bdd.stage.brickauthenticator.UserAuthenticator;
import io.kodokojo.brick.*;
import io.kodokojo.commons.docker.model.ImageName;
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import io.kodokojo.commons.utils.DockerService;
import io.kodokojo.commons.utils.DockerTestApplicationBuilder;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.HttpServiceChecker;
import javaslang.control.Try;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrickConfigurerGiven<SELF extends BrickConfigurerGiven<?>> extends Stage<SELF> implements DockerTestApplicationBuilder {


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

        this.brickName = brickName.toLowerCase();
        this.userAuthenticator = userAuthenticator;

        ImageName imageName = StringToImageNameConverter.convert(image);

        Try<DockerService> dockerServices = startDockerService(dockerTestSupport, brickName, imageName.getName(), imageName.getTag(), port, new HttpServiceChecker(port, timeout * 1000, "/"));
        DockerService service = dockerServices.getOrElseThrow(() -> {
            return new RuntimeException("Unable to start " + brickName);
        });
        containerId = service.getContainerId();
        brickUrl = dockerTestSupport.getHttpContainerUrl(containerId, port);

        LOGGER.info("BrickConfiguration {} successfully started.", brickName);
        brickFactory = new DefaultBrickFactory();
        brickConfigurerProvider = new DefaultBrickConfigurerProvider(new DefaultBrickUrlFactory("kodokojo.dev"), new OkHttpClient());
        return self();
    }


}
