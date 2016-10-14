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


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.squareup.okhttp.OkHttpClient;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.Launcher;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.config.DockerConfig;
import io.kodokojo.config.VersionConfig;
import io.kodokojo.config.module.InjectorProvider;
import io.kodokojo.config.properties.PropertyResolver;
import io.kodokojo.config.properties.provider.*;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.model.Service;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.repository.Repository;
import io.kodokojo.test.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.mockito.Mockito.mock;

public class ApplicationGiven<SELF extends ApplicationGiven<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationGiven.class);

    private static final Map<String, String> USER_PASSWORD = new HashMap<>();

    static {
        USER_PASSWORD.put("jpthiery", "jpascal");
    }

    @ProvidedScenarioState
    public DockerTestSupport dockerTestSupport = new DockerTestSupport();

    @ProvidedScenarioState
    DockerClient dockerClient;

    @ProvidedScenarioState
    String redisHost;

    @ProvidedScenarioState
    int redisPort;

    @ProvidedScenarioState
    HttpEndpoint httpEndpoint;

    @ProvidedScenarioState
    String restEntryPointHost;

    @ProvidedScenarioState
    int restEntryPointPort;

    @ProvidedScenarioState
    Repository repository;

    @ProvidedScenarioState
    String currentUserLogin;

    @ProvidedScenarioState
    String whoAmI;

    @ProvidedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();


    @ProvidedScenarioState
    HttpUserSupport httpUserSupport;

    public SELF redis_is_started(@Hidden DockerTestSupport dockerTestSupport) {

        this.dockerTestSupport = dockerTestSupport;
        dockerClient = dockerTestSupport.getDockerClient();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                LinkedList<PropertyValueProvider> propertyValueProviders = new LinkedList<>();

                propertyValueProviders.add(new SystemPropertyValueProvider());
                propertyValueProviders.add(new SystemEnvValueProvider());
                OrderedMergedValueProvider valueProvider = new OrderedMergedValueProvider(propertyValueProviders);
                PropertyResolver resolver = new PropertyResolver(new DockerConfigValueProvider(valueProvider));

                bind(DockerConfig.class).toInstance(resolver.createProxy(DockerConfig.class));
                bind(VersionConfig.class).toInstance(new VersionConfig() {
                    @Override
                    public String version() {
                        return "1.0.0";
                    }

                    @Override
                    public String gitSha1() {
                        return "1234";
                    }

                    @Override
                    public String branch() {
                        return "test";
                    }
                });


            }
        });
        Launcher.INJECTOR = injector;
        DockerConfig dockerConfig = injector.getInstance(DockerConfig.class);
        redis_is_started();
        return self();
    }

    public SELF redis_is_started() {
        List<Image> images = dockerClient.listImagesCmd().exec();
        boolean foundRedis = false;
        Iterator<Image> iterator = images.iterator();
        while (iterator.hasNext() && !foundRedis) {
            Image image = iterator.next();
            foundRedis = image.getId().equals("redis") && Arrays.asList(image.getRepoTags()).contains("latest");
        }
        if (!foundRedis) {
            LOGGER.info("Pulling docker image redis:latest");
            this.dockerTestSupport.pullImage("redis:latest");
        }

        Service service = StageUtils.startDockerRedis(this.dockerTestSupport);
        redisHost = service.getHost();
        redisPort = service.getPortDefinition().getContainerPort();

        return self();
    }

    public SELF kodokojo_restEntrypoint_is_available() {
        int port = TestUtils.getEphemeralPort();
        return kodokojo_restEntrypoint_is_available_on_port_$(port);
    }

    public SELF kodokojo_is_running(@Hidden DockerTestSupport dockerTestSupport) {
        if (this.dockerTestSupport != null) {
            this.dockerTestSupport.stopAndRemoveContainer();
        }
        this.dockerTestSupport = dockerTestSupport;
        redis_is_started(dockerTestSupport);
        return kodokojo_restEntrypoint_is_available();
    }

    public SELF kodokojo_restEntrypoint_is_available_on_port_$(int port) {

        BrickManager brickManager = mock(BrickManager.class);
        ConfigurationStore configurationStore = mock(ConfigurationStore.class);
        InjectorProvider injectorProvider = new InjectorProvider(null, dockerTestSupport, port, redisHost, redisPort, brickManager, configurationStore);
        Launcher.INJECTOR = injectorProvider.provideInjector();
        repository = Launcher.INJECTOR.getInstance(Repository.class);
        httpEndpoint = Launcher.INJECTOR.getInstance(HttpEndpoint.class);

        httpUserSupport = new HttpUserSupport(new OkHttpClient(), "localhost:" + port);
        httpEndpoint.start();
        restEntryPointPort = port;
        restEntryPointHost = "localhost";

        return self();
    }

    public SELF i_will_be_user_$(@Quoted String username) {
        return i_am_user_$(username, false);
    }

    public SELF i_am_user_$(@Quoted String username, @Hidden boolean createUser) {
        currentUserLogin = username;
        if (createUser) {

            UserInfo userCreated = httpUserSupport.createUser(null, username + "@kodokojo.io");
            currentUsers.put(userCreated.getUsername(), userCreated);
        }
        return self();
    }

    @AfterScenario
    public void tear_down() {
        if (httpEndpoint != null) {
            httpEndpoint.stop();
            httpEndpoint = null;
        }

    }

}
