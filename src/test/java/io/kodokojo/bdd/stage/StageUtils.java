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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import io.kodokojo.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.model.Entity;
import io.kodokojo.model.User;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.UserRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class StageUtils {

    private StageUtils() {
        //
    }

    public static Service startDockerRedis(DockerTestSupport dockerTestSupport) {
        DockerClient dockerClient = dockerTestSupport.getDockerClient();

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(6379);
        portBinding.bind(exposedPort, new Ports.Binding(null, null));

        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("redis:latest")
                .withExposedPorts(exposedPort)
                .withPortBindings(portBinding)
                .exec();
        dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        dockerTestSupport.addContainerIdToClean(createContainerResponse.getId());

        String redisHost = dockerTestSupport.getContainerPublicIp(createContainerResponse.getId());
        int redisPort = dockerTestSupport.getExposedPort(createContainerResponse.getId(), 6379);

        long end = System.currentTimeMillis() + 60000;
        boolean redisIsReady = false;
        while (!redisIsReady && (end - System.currentTimeMillis()) > 0) {
            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
            try (Jedis jedis = jedisPool.getResource()) {
                String resPing = jedis.ping();
                redisIsReady = "PONG".equals(resPing);
            } catch (JedisConnectionException e) {
                //  Silently ignore, Redis may not be available
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertThat(redisIsReady).isTrue();
        return new Service("redis", redisHost, redisPort);
    }

}
