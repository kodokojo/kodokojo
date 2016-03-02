package io.kodokojo.user;

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
import io.kodokojo.bdd.Back;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.model.User;
import io.kodokojo.commons.model.UserService;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.user.redis.RedisUserManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Back
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
        User jpthiery = new User(userManager.generateId(),"Jean-Pascal THIERY", "jpthiery", email, "jpascal", RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email));

        userManager.addUser(jpthiery);

        User user = userManager.getUserByUsername("jpthiery");

        assertThat(user).isNotNull();
    }

    @Test
    @DockerIsRequire
    public void add_user_service() {
        UserManager userManager = new RedisUserManager(aesKey, redisHost, redisPort);
        UserService jenkins = new UserService(userManager.generateId(), "jenkins", "jenkins", "jenkins", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        userManager.addUserService(jenkins);

        UserService userService = userManager.getUserServiceByName("jenkins");

        assertThat(userService).isNotNull();
    }

}