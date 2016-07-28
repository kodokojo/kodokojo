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
import com.github.dockerjava.api.model.Image;
import com.google.inject.*;
import com.squareup.okhttp.OkHttpClient;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.Launcher;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.brick.DefaultBrickUrlFactory;
import io.kodokojo.config.DockerConfig;
import io.kodokojo.config.properties.provider.*;
import io.kodokojo.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.config.properties.PropertyResolver;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.EmailConfig;
import io.kodokojo.config.module.EmailSenderModule;
import io.kodokojo.config.module.endpoint.BrickEndpointModule;
import io.kodokojo.config.module.endpoint.ProjectEndpointModule;
import io.kodokojo.config.module.endpoint.UserEndpointModule;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.endpoint.SparkEndpoint;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.redis.RedisEntityRepository;
import io.kodokojo.service.redis.RedisProjectRepository;
import io.kodokojo.service.redis.RedisUserRepository;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserRepository;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.authentification.SimpleUserAuthenticator;
import io.kodokojo.test.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.assertj.core.api.Assertions.fail;
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
    RedisUserRepository userStore;

    @ProvidedScenarioState
    String currentUserLogin;

    @ProvidedScenarioState
    String whoAmI;

    @ProvidedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();

    @ProvidedScenarioState
    ProjectManager projectManager;

    @ProvidedScenarioState
    ProjectRepository projectRepository;

    @ProvidedScenarioState
    EntityRepository entityRepository;

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
        redisPort = service.getPort();

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
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            SecretKey aesKey = generator.generateKey();
            userStore = new RedisUserRepository(aesKey, redisHost, redisPort);
            DefaultBrickFactory brickFactory = new DefaultBrickFactory();
            projectRepository = new RedisProjectRepository(aesKey, redisHost, redisPort, brickFactory);
            entityRepository = new RedisEntityRepository(aesKey, redisHost, redisPort);
            UserAuthenticator<SimpleCredential> userAuthenticator = new SimpleUserAuthenticator(userStore);
            projectManager = mock(ProjectManager.class);
            Launcher.INJECTOR = Guice.createInjector(new UserEndpointModule(), new ProjectEndpointModule(), new BrickEndpointModule(), new EmailSenderModule(), new AbstractModule() {
                @Override
                protected void configure() {
                    bind(UserRepository.class).toInstance(userStore);
                    bind(ProjectRepository.class).toInstance(projectRepository);
                    bind(ProjectManager.class).toInstance(projectManager);
                    bind(EntityRepository.class).toInstance(entityRepository);
                    bind(Key.get(new TypeLiteral<UserAuthenticator<SimpleCredential>>() {
                    })).toInstance(userAuthenticator);
                    bind(BrickFactory.class).toInstance(brickFactory);
                    bind(ApplicationConfig.class).toInstance(new ApplicationConfig() {
                        @Override
                        public int port() {
                            return 80;
                        }

                        @Override
                        public String domain() {
                            return "kodokojo.dev";
                        }

                        @Override
                        public String loadbalancerHost() {
                            return dockerTestSupport.getServerIp();
                        }

                        @Override
                        public int initialSshPort() {
                            return 1022;
                        }

                        @Override
                        public long sslCaDuration() {
                            return -1;
                        }
                    });
                    bind(BrickUrlFactory.class).toInstance(new DefaultBrickUrlFactory("kodokojo.dev"));
                    bind(EmailConfig.class).toInstance(new EmailConfig() {
                        @Override
                        public String smtpHost() {
                            return null;
                        }

                        @Override
                        public int smtpPort() {
                            return 0;
                        }

                        @Override
                        public String smtpUsername() {
                            return null;
                        }

                        @Override
                        public String smtpPassword() {
                            return null;
                        }

                        @Override
                        public String smtpFrom() {
                            return null;
                        }

                    });
                }
            });
            Set<SparkEndpoint> sparkEndpoints = Launcher.INJECTOR.getInstance(Key.get(new TypeLiteral<Set<SparkEndpoint>>() {
            }));
            httpEndpoint = new HttpEndpoint(port, new SimpleUserAuthenticator(userStore), sparkEndpoints);
            httpUserSupport = new HttpUserSupport(new OkHttpClient(), "localhost:" + port);
            httpEndpoint.start();
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

            UserInfo userCreated = httpUserSupport.createUser(null, username + "@kodokojo.io");
            currentUsers.put(userCreated.getUsername(), userCreated);
            /*

            //TODO Use REST API instead.
            String identifier = userStore.generateId();
            String password = USER_PASSWORD.get(username) == null ? new BigInteger(130, new SecureRandom()).toString(32) : USER_PASSWORD.get(username);

            try {
                KeyPair keyPair = RSAUtils.generateRsaKeyPair();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                String email = username + "@kodokojo.io";

                User user = new User(identifier, username, username, email, password, RSAUtils.encodePublicKey(publicKey, email));

                Entity entity = new Entity(user.getUsername(), user);
                String entityName = entityRepository.addEntity(entity);
                entityRepository.addUserToEntity(user.getIdentifier(), entityName);
                user = new User(user.getIdentifier(), entityName, user.getFirstName(), user.getLastName(), username, email, password, user.getSshPublicKey());
                boolean userAdded = userStore.addUser(user);
                assertThat(userAdded).isTrue();
                whoAmI = username;
                currentUsers.put(currentUserLogin, new UserInfo(currentUserLogin, identifier, password, email));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            */
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
