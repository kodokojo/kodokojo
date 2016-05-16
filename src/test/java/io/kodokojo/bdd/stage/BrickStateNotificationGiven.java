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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.Launcher;
import io.kodokojo.brick.*;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.module.ActorModule;
import io.kodokojo.entrypoint.RestEntryPoint;
import io.kodokojo.model.User;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.redis.RedisEntityStore;
import io.kodokojo.service.redis.RedisProjectStore;
import io.kodokojo.service.redis.RedisUserStore;
import io.kodokojo.service.store.EntityStore;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.user.SimpleUserAuthenticator;
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

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class BrickStateNotificationGiven<SELF extends BrickStateNotificationGiven<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickStateNotificationGiven.class);

    @ProvidedScenarioState
    public DockerTestSupport dockerTestSupport = new DockerTestSupport();

    @ProvidedScenarioState
    RestEntryPoint restEntryPoint;

    @ProvidedScenarioState
    ConfigurationStore configurationStore;

    @ProvidedScenarioState
    BootstrapConfigurationProvider bootstrapProvider;

    @ProvidedScenarioState
    String entryPointUrl;

    @ProvidedScenarioState
    User currentUser;

    @ProvidedScenarioState
    BrickManager brickManager;

    @ProvidedScenarioState
    DnsManager dnsManager;

    public SELF kodokojo_is_started() {
        LOGGER.info("Pulling docker image redis:latest");
        dockerTestSupport.pullImage("redis:latest");
        Service service = StageUtils.startDockerRedis(dockerTestSupport);

        brickManager = mock(BrickManager.class);
        bootstrapProvider = mock(BootstrapConfigurationProvider.class);
        dnsManager = mock(DnsManager.class);
        configurationStore = mock(ConfigurationStore.class);

        Mockito.when(bootstrapProvider.provideLoadBalancerIp(anyString(),anyString())).thenReturn("192.168.22.3");
        Mockito.when(bootstrapProvider.provideSshPortEntrypoint(anyString(),anyString())).thenReturn(10022);

        SecretKey tmpKey = null;
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            tmpKey = kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        final SecretKey secreteKey = tmpKey;

        int port = TestUtils.getEphemeralPort();

        RedisUserStore redisUserManager = new RedisUserStore(secreteKey, service.getHost(), service.getPort());
        RedisProjectStore redisProjectStore = new RedisProjectStore(secreteKey, service.getHost(), service.getPort(), new DefaultBrickFactory());
        RedisEntityStore redisEntityStore = new RedisEntityStore(secreteKey, service.getHost(), service.getPort());
        Injector injector = Guice.createInjector(new ActorModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserStore.class).toInstance(redisUserManager);
                bind(ProjectStore.class).toInstance(redisProjectStore);
                bind(EntityStore.class).toInstance(redisEntityStore);
                bind(BrickStateMsgDispatcher.class).toInstance(new BrickStateMsgDispatcher());
                bind(BrickManager.class).toInstance(brickManager);
                bind(DnsManager.class).toInstance(dnsManager);
                bind(ConfigurationStore.class).toInstance(configurationStore);
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
                    public String defaultLoadbalancerIp() {
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
                bind(BrickUrlFactory.class).toInstance(new DefaultBrickUrlFactory("kodokojo.dev"));
            }
        });
        Launcher.INJECTOR = injector;

        entryPointUrl = "localhost:" + port;
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        SSLKeyPair caKey = SSLUtils.createSelfSignedSSLKeyPair("Fake CA", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        DefaultProjectManager projectManager = new DefaultProjectManager(caKey, "kodokojo.dev", configurationStore, redisProjectStore, bootstrapProvider, dnsManager, injector.getInstance(BrickConfigurationStarter.class), new DefaultBrickUrlFactory("kodokojo.dev"), 10000000);
        restEntryPoint = new RestEntryPoint(port, injector.getInstance(UserStore.class), new SimpleUserAuthenticator(redisUserManager),redisEntityStore, redisProjectStore, projectManager,new DefaultBrickFactory());
        restEntryPoint.start();
        return self();
    }

    public SELF i_am_user_$(@Quoted String username) {
        currentUser = StageUtils.createUser(username, Launcher.INJECTOR.getInstance(UserStore.class), Launcher.INJECTOR.getInstance(EntityStore.class));
        return self();
    }

    @AfterScenario
    public void tear_down() {
        dockerTestSupport.stopAndRemoveContainer();
        if (restEntryPoint != null) {
            restEntryPoint.stop();
            restEntryPoint = null;
        }
    }
}
