package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.Stack;
import io.kodokojo.project.starter.BrickManager;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.NoOpDnsManager;
import org.mockito.Mockito;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectManagerGiven<SELF extends ProjectManagerGiven<?>> extends Stage<SELF> {

    @ProvidedScenarioState
    DefaultProjectManager projectManager;

    @ProvidedScenarioState
    BrickManager brickManager;

    @ProvidedScenarioState
    ConfigurationStore configurationStore;

    @ProvidedScenarioState
    ProjectStore projectStore;

    @ProvidedScenarioState
    String projectName;

    @ProvidedScenarioState
    BootstrapConfigurationProvider configProvider;

    @ProvidedScenarioState
    ExecutorService executorService;

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
        Mockito.when(brickManager.getOrchestratorType()).thenReturn(Stack.OrchestratorType.MARATHON);
        configurationStore = mock(ConfigurationStore.class);
        projectStore = mock(ProjectStore.class);
        Mockito.when(projectStore.projectNameIsValid(projectName)).thenReturn(true);
        configProvider = mock(BootstrapConfigurationProvider.class);
        Mockito.when(configProvider.provideLoadBalancerIp(anyString(),anyString())).thenReturn("127.0.0.1");
        Mockito.when(configProvider.provideSshPortEntrypoint(anyString(),anyString())).thenReturn(10022);
        executorService = mock(ExecutorService.class);

        projectManager = new DefaultProjectManager(caKey, "kodokojo.dev", new NoOpDnsManager(), brickManager, configurationStore, projectStore, configProvider, executorService,300000);

        return self();
    }
}

