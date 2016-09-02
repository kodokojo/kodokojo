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
package io.kodokojo.service.redis;

import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.repository.store.ProjectConfigurationStoreModel;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.service.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.model.Stack;
import org.junit.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class RedisProjectStoreIntTest {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    private DockerTestSupport dockerTestSupport;

    private RedisProjectStore redisProjectStore;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        dockerTestSupport = dockerPresentMethodRule.getDockerTestSupport();
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        SecretKey aesKey = kg.generateKey();
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

        List<String> users = new ArrayList<>();
        users.add("1234");

        ProjectConfigurationStoreModel projectConfiguration = createProjectConfiguration(users);

        String identifier = redisProjectStore.addProjectConfiguration(projectConfiguration);

        assertThat(identifier).isNotEmpty();
        Project project = createProject();

        String projectIdentifier = redisProjectStore.addProject(project, identifier);


        ProjectConfigurationStoreModel result = redisProjectStore.getProjectConfigurationById(identifier);
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


    private ProjectConfigurationStoreModel createProjectConfiguration(List<String> users) {
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();
        brickConfigurations.add(new BrickConfiguration("jenkins", BrickType.CI, "1.651", Collections.singleton(new PortDefinition(8080))));
        stackConfigurations.add(new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, "127.0.0.1", 10022));
        return new ProjectConfigurationStoreModel("123456",null, "acme-a", users, stackConfigurations, users);
    }

    Project createProject() throws NoSuchAlgorithmException {
        KeyPair keyPair = RSAUtils.generateRsaKeyPair();
        SSLKeyPair sslKeyPair = SSLUtils.createSelfSignedSSLKeyPair("Acme", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        Set<Stack> stacks = new HashSet<>();
        Set<BrickStateEvent> brickStateEvents = new HashSet<>();
        brickStateEvents.add(new BrickStateEvent("123456", "build-A", BrickType.CI.name(), "jenkins", BrickStateEvent.State.RUNNING, "1.651"));
        stacks.add(new Stack("build-A", StackType.BUILD, brickStateEvents));
        return new Project("123456", "Acme", new Date(), stacks);
    }

}