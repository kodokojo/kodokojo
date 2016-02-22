package io.kodokojo.user;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.project.model.UserService;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RedisUserManagerIntTest {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    private String redisHost;

    private int redisPort;

    private SecretKey aesKey;

    private KeyPair keyPair;

    @Before
    public void setup() {
        DockerTestSupport dockerTestSupport = dockerPresentMethodRule.getDockerTestSupport();
        DockerClient dockerClient = dockerTestSupport.getDockerClient();
        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("redis:latest").withExposedPorts(ExposedPort.tcp(6379)).withPortBindings(new Ports(ExposedPort.tcp(6379), Ports.Binding(null))).exec();
        dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        dockerTestSupport.addContainerIdToClean(createContainerResponse.getId());
        redisHost = dockerTestSupport.getServerIp();
        redisPort = dockerTestSupport.getExposedPort(createContainerResponse.getId(), 6379);

        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            aesKey = generator.generateKey();
            try {
                keyPair = RSAUtils.generateRsaKeyPair();
            } catch (NoSuchAlgorithmException e) {
                fail("Unable to generate RSA key", e);
            }
        } catch (NoSuchAlgorithmException e) {
            fail("unable to generate an AES key", e);
        }
    }

    @Test
    @DockerIsRequire
    public void add_user() {

        UserManager userManager = new RedisUserManager(aesKey, redisHost, redisPort);

        String email = "jpthiery@xebia.fr";
        User jpthiery = new User("Jean-Pascal THIERY", "jpthiery", email, "jpascal", RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email));

        userManager.addUser(jpthiery);

        User user = userManager.getUserByUsername("jpthiery");

        assertThat(user).isNotNull();
    }

    @Test
    @DockerIsRequire
    public void add_user_service() {
        UserManager userManager = new RedisUserManager(aesKey, redisHost, redisPort);
        UserService jenkins = new UserService("jenkins", "jenkins", "jenkins", "jenkins", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        userManager.addUserService(jenkins);

        UserService userService = userManager.getUserServiceByName("jenkins");

        assertThat(userService).isNotNull();
    }

}