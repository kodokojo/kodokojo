package io.kodokojo.bdd.stage;

/*
 * #%L
 * project-manager
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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.Launcher;
import io.kodokojo.commons.config.DockerConfig;
import io.kodokojo.model.User;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.*;
import io.kodokojo.entrypoint.RestEntrypoint;
import io.kodokojo.service.*;
import io.kodokojo.service.redis.RedisBootstrapConfigurationProvider;
import io.kodokojo.service.redis.RedisProjectStore;
import io.kodokojo.service.user.SimpleCredential;
import io.kodokojo.service.user.SimpleUserAuthenticator;
import io.kodokojo.service.user.redis.RedisUserManager;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class ApplicationGiven <SELF extends ApplicationGiven<?>> extends Stage<SELF> {

    private static final Map<String, String> USER_PASSWORD = new HashMap<>();

    static {
        USER_PASSWORD.put("jpthiery", "jpascal");
    }

    @Rule
    @ProvidedScenarioState
    public DockerTestSupport dockerTestSupport = new DockerTestSupport();

    @ProvidedScenarioState
    DockerClient dockerClient;

    @ProvidedScenarioState
    String redisHost;

    @ProvidedScenarioState
    int redisPort;

    @ProvidedScenarioState
    RestEntrypoint restEntrypoint;

    @ProvidedScenarioState
    String restEntryPointHost;

    @ProvidedScenarioState
    int restEntryPointPort;

    @ProvidedScenarioState
    RedisUserManager userManager;

    @ProvidedScenarioState
    String currentUserLogin;

    @ProvidedScenarioState
    String whoAmI;

    @ProvidedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();

    @ProvidedScenarioState
    ProjectManager projectManager;

    @ProvidedScenarioState
    ProjectStore projectStore;

    @BeforeScenario
    public void create_a_docker_client() {
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
            }
        });
        Launcher.INJECTOR = injector;
        DockerConfig dockerConfig = injector.getInstance(DockerConfig.class);
    }

    public SELF redis_is_started() {
        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("redis:latest").withExposedPorts(ExposedPort.tcp(6379)).withPortBindings(new Ports(ExposedPort.tcp(6379), Ports.Binding(null))).exec();
        dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        dockerTestSupport.addContainerIdToClean(createContainerResponse.getId());
        redisHost = dockerTestSupport.getServerIp();
        redisPort = dockerTestSupport.getExposedPort(createContainerResponse.getId(), 6379);
        return self();
    }

    public SELF kodokojo_restEntrypoint_is_available() {
        int port = 0;
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            port = ((InetSocketAddress )serverSocket.getLocalSocketAddress()).getPort();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kodokojo_restEntrypoint_is_available_on_port_$(port);
    }

    public SELF kodokojo_is_running() {
        redis_is_started();
        return kodokojo_restEntrypoint_is_available();
    }

    public SELF kodokojo_restEntrypoint_is_available_on_port_$(int port) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            SecretKey aesKey = generator.generateKey();
            userManager = new RedisUserManager(aesKey, redisHost, redisPort);
            projectStore = new RedisProjectStore(aesKey, redisHost, redisPort, new DefaultBrickFactory(null));
            UserAuthenticator<SimpleCredential> userAuthenticator = new SimpleUserAuthenticator(userManager);
            projectManager = mock(ProjectManager.class);
            restEntrypoint = new RestEntrypoint(port, userManager,userAuthenticator,projectStore, projectManager, new DefaultBrickFactory(null));
            Launcher.INJECTOR = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(UserManager.class).toInstance(userManager);
                }
            });
            restEntrypoint.start();
            restEntryPointPort = port;
            restEntryPointHost = "localhost";
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage(), e);
        }
        return self();
    }
    public SELF i_will_be_user_$(@Quoted String username) {
        return i_am_user_$(username, false);
    }
    public SELF i_am_user_$(@Quoted String username, @Hidden boolean createUser) {
        currentUserLogin = username;
        if (createUser) {
            String identifier = userManager.generateId();
            String password = USER_PASSWORD.get(username) == null ? new BigInteger(130, new SecureRandom()).toString(32) : USER_PASSWORD.get(username);

            try {
                KeyPair keyPair = RSAUtils.generateRsaKeyPair();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                String email = username + "@kodokojo.io";
                boolean userAdded = userManager.addUser(new User(identifier, username, username, email, password, RSAUtils.encodePublicKey(publicKey, email)));
                assertThat(userAdded).isTrue();
                whoAmI = username;
                currentUsers.put(currentUserLogin, new UserInfo(currentUserLogin, identifier, password, email));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return self();
    }

    @AfterScenario
    public void tear_down() {
        dockerTestSupport.stopAndRemoveContainer();
        if (restEntrypoint != null) {
            restEntrypoint.stop();
            restEntrypoint = null;
        }
    }

}
