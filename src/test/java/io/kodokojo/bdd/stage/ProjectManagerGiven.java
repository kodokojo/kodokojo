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

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.brick.DefaultBrickConfigurerProvider;
import io.kodokojo.brick.DefaultBrickUrlFactory;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.NoOpDnsManager;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.service.ssl.SSLUtils;
import org.mockito.Mockito;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectManagerGiven<SELF extends ProjectManagerGiven<?>> extends Stage<SELF> {

    @ProvidedScenarioState
    ActorSystem actorSystem;

    @ProvidedScenarioState
    List<BrickStartContext> brickCaptured;

    @ProvidedScenarioState
    DefaultProjectManager projectManager;

    @ProvidedScenarioState
    BrickManager brickManager;

    @ProvidedScenarioState
    ConfigurationStore configurationStore;

    @ProvidedScenarioState
    ProjectRepository projectRepository;

    @ProvidedScenarioState
    String projectName;

    @ProvidedScenarioState
    BootstrapConfigurationProvider configProvider;

    public SELF i_bootstrap_project_$(String projectName) {
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        this.projectName = projectName;
        RSAPrivateKey caPrivate = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey caPublic = (RSAPublicKey) keyPair.getPublic();
        SSLKeyPair caKey = SSLUtils.createSelfSignedSSLKeyPair("Fake Root " + projectName, caPrivate, caPublic);

        brickManager = mock(BrickManager.class);
        configurationStore = mock(ConfigurationStore.class);
        projectRepository = mock(ProjectRepository.class);
        Mockito.when(projectRepository.projectNameIsValid(projectName)).thenReturn(true);
        configProvider = mock(BootstrapConfigurationProvider.class);
        Mockito.when(configProvider.provideLoadBalancerHost(anyString(), anyString())).thenReturn("127.0.0.1");
        Mockito.when(configProvider.provideSshPortEntrypoint(anyString(), anyString())).thenReturn(10022);
        actorSystem = ActorSystem.create("test");
        brickCaptured = new ArrayList<>();
        ActorRef actor = actorSystem.actorOf(Props.create(TestActor.class, brickCaptured), "endpoint");
        //Mockito.when(actor.tell(any(), any()))


        projectManager = new DefaultProjectManager("kodokojo.dev", configurationStore, projectRepository, configProvider, new NoOpDnsManager(), new DefaultBrickConfigurerProvider(new DefaultBrickUrlFactory("kodokojo.dev"), new OkHttpClient()), actorSystem, new DefaultBrickUrlFactory("kodokojo.dev"));

        return self();
    }

    public static class TestActor extends AbstractActor {

        public TestActor(List<BrickStartContext> captured) {
            receive(ReceiveBuilder.match(BrickStartContext.class, msg -> {
                captured.add(msg);
            }).build());
        }

    }
}

