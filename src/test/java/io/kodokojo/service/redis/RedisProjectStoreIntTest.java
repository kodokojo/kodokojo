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
package io.kodokojo.service.redis;

import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.model.Stack;
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

    private DockerTestSupport dockerTestSupport;

    private RedisProjectStore redisProjectStore;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        dockerTestSupport = dockerPresentMethodRule.getDockerTestSupport();
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey aesKey = generator.generateKey();
        Service service = StageUtils.startDockerRedis(dockerTestSupport);
        String redisHost = service.getHost();
        int redisPort = service.getPort();
        redisProjectStore = new RedisProjectStore(aesKey, redisHost, redisPort, new DefaultBrickFactory());
    }

    @After
    public void tearDown() {
        redisProjectStore.stop();
    }
/*
    @Test
    @DockerIsRequire
    public void add_valid_entity() throws NoSuchAlgorithmException {
        Entity entity = new Entity("MaBoite", true, new User("12345", "Jean-Pascal THIERY", "jpthiery", "jpthiery@xebia.fr", "jpthiery", "an SSH key"));

        String identifier = redisProjectStore.addEntity(entity);

        assertThat(identifier).isNotEmpty();

        Entity result = redisProjectStore.getEntityById(identifier);

        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo(identifier);
        String entityOfUserId = redisProjectStore.getEntityIdOfUserId("12345");
        assertThat(entityOfUserId).isEqualTo(identifier);
    }
*/

    @Test
    @DockerIsRequire
    public void add_valid_project() throws NoSuchAlgorithmException {

        List<User> users = new ArrayList<>();
        User owner = new User("1234", "Jpascal", "jpthiery", "jpthiery@kodokojo.io", "mysecretpassword", "ssh public key");
        users.add(owner);

        ProjectConfiguration projectConfiguration = createProjectConfiguration(users);

        String identifier = redisProjectStore.addProjectConfiguration(projectConfiguration);

        assertThat(identifier).isNotEmpty();
        Project project = createProject();

        String projectIdentifier = redisProjectStore.addProject(project, identifier);


        ProjectConfiguration result = redisProjectStore.getProjectConfigurationById(identifier);
        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo(identifier);
        assertThat(result.getEntityIdentifier()).isNotNull();

        Set<String> projectConfigIdsByUserIdentifier = redisProjectStore.getProjectConfigIdsByUserIdentifier("1234");
        assertThat(projectConfigIdsByUserIdentifier).isNotNull();
        assertThat(projectConfigIdsByUserIdentifier).contains(result.getIdentifier());


        assertThat(projectIdentifier).isNotEmpty();

        Project projectResult = redisProjectStore.getProjectByIdentifier(projectIdentifier);

        assertThat(projectResult).isNotNull();
        assertThat(projectResult.getIdentifier()).isEqualTo(projectIdentifier);


    }


    private ProjectConfiguration createProjectConfiguration(List<User> users) {
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();
        brickConfigurations.add(new BrickConfiguration(new Brick("jenkins", BrickType.CI, "1.651")));
        stackConfigurations.add(new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, "127.0.0.1", 10022));
        return new ProjectConfiguration("123456", "acme-a", users, stackConfigurations, users);
    }

    Project createProject() throws NoSuchAlgorithmException {
        KeyPair keyPair = RSAUtils.generateRsaKeyPair();
        SSLKeyPair sslKeyPair = SSLUtils.createSelfSignedSSLKeyPair("Acme", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        Set<Stack> stacks = new HashSet<>();
        Set<BrickState> brickStates = new HashSet<>();
        brickStates.add(new BrickState("123456", "build-A", BrickType.CI.name(), "jenkins", BrickState.State.RUNNING, "1.651"));
        stacks.add(new Stack("build-A", StackType.BUILD, brickStates));
        return new Project("123456", "Acme", sslKeyPair, new Date(), stacks);
    }

}