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

import com.google.inject.*;
import com.squareup.okhttp.OkHttpClient;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.Launcher;
import io.kodokojo.brick.*;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.EmailConfig;
import io.kodokojo.config.module.ActorModule;
import io.kodokojo.config.module.AkkaModule;
import io.kodokojo.config.module.EmailSenderModule;
import io.kodokojo.config.module.endpoint.ProjectEndpointModule;
import io.kodokojo.config.module.endpoint.UserEndpointModule;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.endpoint.SparkEndpoint;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.model.Service;
import io.kodokojo.service.*;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.authentification.SimpleUserAuthenticator;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.redis.RedisEntityStore;
import io.kodokojo.service.redis.RedisProjectStore;
import io.kodokojo.service.redis.RedisUserRepository;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.Repository;
import io.kodokojo.service.repository.UserRepository;
import io.kodokojo.service.repository.store.EntityStore;
import io.kodokojo.service.repository.store.ProjectStore;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.service.ssl.SSLUtils;
import io.kodokojo.service.ssl.WildcardSSLCertificatProvider;
import io.kodokojo.test.utils.TestUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class BrickStateNotificationGiven<SELF extends BrickStateNotificationGiven<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickStateNotificationGiven.class);

    @ProvidedScenarioState
    public DockerTestSupport dockerTestSupport;

    @ProvidedScenarioState
    HttpEndpoint httpEndpoint;

    @ProvidedScenarioState
    ConfigurationStore configurationStore;

    @ProvidedScenarioState
    BootstrapConfigurationProvider bootstrapProvider;

    @ProvidedScenarioState
    String entryPointUrl;

    @ProvidedScenarioState
    UserInfo currentUser;

    @ProvidedScenarioState
    BrickManager brickManager;

    @ProvidedScenarioState
    DnsManager dnsManager;

    @ProvidedScenarioState
    HttpUserSupport httpUserSupport;

    public SELF kodokojo_is_started(@Hidden DockerTestSupport dockerTestSupport) {
        if (this.dockerTestSupport != null) {
            this.dockerTestSupport.stopAndRemoveContainer();
        }
        this.dockerTestSupport = dockerTestSupport;
        LOGGER.info("Pulling docker image redis:latest");
        this.dockerTestSupport.pullImage("redis:latest");
        Service service = StageUtils.startDockerRedis(this.dockerTestSupport);

        brickManager = mock(BrickManager.class);
        bootstrapProvider = mock(BootstrapConfigurationProvider.class);
        dnsManager = mock(DnsManager.class);
        configurationStore = mock(ConfigurationStore.class);

        Mockito.when(bootstrapProvider.provideLoadBalancerHost(anyString(), anyString())).thenReturn("192.168.22.3");
        Mockito.when(bootstrapProvider.provideSshPortEntrypoint(anyString(), anyString())).thenReturn(10022);

        SecretKey tmpKey = null;
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            tmpKey = kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        final SecretKey secreteKey = tmpKey;

        int port = TestUtils.getEphemeralPort();

        RedisUserRepository redisUserManager = new RedisUserRepository(secreteKey, service.getHost(), service.getPort());
        RedisProjectStore redisProjectStore = new RedisProjectStore(secreteKey, service.getHost(), service.getPort(), new DefaultBrickFactory());
        RedisEntityStore redisEntityStore = new RedisEntityStore(secreteKey, service.getHost(), service.getPort());
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        SSLKeyPair caKey = SSLUtils.createSelfSignedSSLKeyPair("Fake CA", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        Repository repository = new Repository(redisUserManager, redisUserManager, redisEntityStore, redisProjectStore);
        Injector injector = Guice.createInjector(new EmailSenderModule(), new UserEndpointModule(), new ProjectEndpointModule(), new ActorModule(), new AkkaModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserRepository.class).toInstance(redisUserManager);
                bind(ProjectStore.class).toInstance(redisProjectStore);
                bind(EntityStore.class).toInstance(redisEntityStore);
                bind(Repository.class).toInstance(repository);
                bind(ProjectRepository.class).toInstance(repository);
                bind(EntityRepository.class).toInstance(repository);
                bind(BrickStateMsgDispatcher.class).toInstance(new BrickStateMsgDispatcher());
                bind(BrickManager.class).toInstance(brickManager);
                bind(DnsManager.class).toInstance(dnsManager);
                bind(ConfigurationStore.class).toInstance(configurationStore);
                bind(BrickFactory.class).toInstance(new DefaultBrickFactory());
                bind(Key.get(new TypeLiteral<UserAuthenticator<SimpleCredential>>() {
                })).toInstance(new SimpleUserAuthenticator(redisUserManager));
                DefaultBrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
                bind(BrickConfigurerProvider.class).toInstance(new DefaultBrickConfigurerProvider(brickUrlFactory, new OkHttpClient()));
                bind(ApplicationConfig.class).toInstance(new ApplicationConfig() {
                    @Override
                    public int port() {
                        return port;
                    }

                    @Override
                    public String domain() {
                        return "kodokojo.dev";
                    }

                    @Override
                    public String loadbalancerHost() {
                        return "192.168.22.3";
                    }

                    @Override
                    public int initialSshPort() {
                        return 10022;
                    }

                    @Override
                    public long sslCaDuration() {
                        return -1;
                    }
                });
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
                bind(SSLCertificatProvider.class).toInstance(new WildcardSSLCertificatProvider(caKey));
                bind(BrickUrlFactory.class).toInstance(brickUrlFactory);
            }

            @Provides
            @Singleton
            ProjectManager provideProjectManager(BrickConfigurationStarter brickConfigurationStarter, BrickConfigurerProvider brickConfigurerProvider, BrickUrlFactory brickUrlFactory) {
                return new DefaultProjectManager("kodokojo.dev", configurationStore, repository, bootstrapProvider, dnsManager, brickConfigurerProvider, brickConfigurationStarter, brickUrlFactory);
            }

        });
        //    DefaultProjectManager projectManager = new DefaultProjectManager(caKey, "kodokojo.dev", configurationStore, redisProjectStore, bootstrapProvider, dnsManager, injector.getInstance(BrickConfigurerProvider.class), injector.getInstance(BrickConfigurationStarter.class), new DefaultBrickUrlFactory("kodokojo.dev"), 10000000);

        Launcher.INJECTOR = injector;

        entryPointUrl = "localhost:" + port;
        Set<SparkEndpoint> sparkEndpoints = Launcher.INJECTOR.getInstance(Key.get(new TypeLiteral<Set<SparkEndpoint>>() {
        }));
        httpEndpoint = new HttpEndpoint(port, new SimpleUserAuthenticator(redisUserManager), sparkEndpoints);
        httpUserSupport = new HttpUserSupport(new OkHttpClient(), entryPointUrl);
        httpEndpoint.start();
        return self();
    }

    public SELF i_am_user_$(@Quoted String username) {
        //currentUser = StageUtils.createUser(username, Launcher.INJECTOR.getInstance(UserRepository.class), Launcher.INJECTOR.getInstance(EntityRepository.class));
        currentUser = httpUserSupport.createUser(null, username + "@kodokojo.dev");
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
