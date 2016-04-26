package io.kodokojo.service.redis;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.model.Stack;
import io.kodokojo.service.DefaultBrickFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisProjectStoreIntTest {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    private DockerTestSupport dockerTestSupport = new DockerTestSupport();

    private RedisProjectStore redisProjectStore;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey aesKey = generator.generateKey();
        DockerClient dockerClient = dockerTestSupport.getDockerClient();
        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("redis:latest").withExposedPorts(ExposedPort.tcp(6379)).withPortBindings(new Ports(ExposedPort.tcp(6379), Ports.Binding(null))).exec();
        dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        dockerTestSupport.addContainerIdToClean(createContainerResponse.getId());
        String redisHost = dockerTestSupport.getServerIp();
        int redisPort = dockerTestSupport.getExposedPort(createContainerResponse.getId(), 6379);
        redisProjectStore = new RedisProjectStore(aesKey, redisHost, redisPort, new DefaultBrickFactory(null));
    }

    @After
    public void tearDown() {
        dockerTestSupport.stopAndRemoveContainer();
        redisProjectStore.stop();
    }

    @Test
    @DockerIsRequire
    public void add_valid_project() throws NoSuchAlgorithmException {
        Project project = createProject();

        String identifier = redisProjectStore.addProject(project);

        assertThat(identifier).isNotEmpty();

        Project result = redisProjectStore.getProjectByName(project.getName());

        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo(identifier);
    }

    @Test
    @DockerIsRequire
    public void add_valid_project_configuration() {
        List<User> users = new ArrayList<>();
        User owner = new User("1234", "Jpascal", "jpthiery", "jpthiery@kodokojo.io", "mysecretpassword", "ssh public key");

        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();
        brickConfigurations.add(new BrickConfiguration(new Brick("jenkins", BrickType.CI)));
        stackConfigurations.add(new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, "127.0.0.1", 10022));
        ProjectConfiguration projectConfiguration = new ProjectConfiguration("acme-a", owner, stackConfigurations,users);

        String identifier = redisProjectStore.addProjectConfiguration(projectConfiguration);
        assertThat(identifier).isNotEmpty();

        ProjectConfiguration result = redisProjectStore.getProjectConfigurationById(identifier);
        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo(identifier);
    }

    Project createProject() throws NoSuchAlgorithmException {
        KeyPair keyPair = RSAUtils.generateRsaKeyPair();
        SSLKeyPair sslKeyPair = SSLUtils.createSelfSignedSSLKeyPair("Acme", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        Set<Stack> stacks = new HashSet<>();
        Set<BrickDeploymentState> brickEntities = new HashSet<>();
        List<Service> services = new ArrayList<>();
        services.add(new Service("fake-80", "localhost", 80));
        brickEntities.add(new BrickDeploymentState(new Brick("fake", BrickType.CI), services, 1));
        stacks.add(new Stack("build-A", StackType.BUILD, brickEntities));
        return new Project("Acme", sslKeyPair, new Date(), stacks);
    }

}