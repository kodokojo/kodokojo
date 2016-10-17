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

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RedisProjectStoreTest {

    private RedisProjectStore redisProjectStore;

    private JedisPool jedisPool;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");

        jedisPool = mock(JedisPool.class);
        redisProjectStore = new RedisProjectStore(kg.generateKey(), "localhost", 6379) {
            @Override
            protected JedisPool createJedisPool(String host, int port) {
                return jedisPool;
            }
        };
    }

    @Test
    public void project_name_valid() {
        String projectName = "Acme-12_aplha";

        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists((byte[]) any())).thenReturn(false);

        boolean isValid = redisProjectStore.projectNameIsValid(projectName);
        assertThat(isValid).isTrue();
    }

    @Test
    public void project_name_invalid() {
        String projectName = "Acme-12!";
        boolean isValid = redisProjectStore.projectNameIsValid(projectName);
        assertThat(isValid).isFalse();
    }

    @Test
    public void project_name_already_exist() {
        String projectName = "Acme-12_aplha";

        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists((byte[]) any())).thenReturn(true);

        boolean isValid = redisProjectStore.projectNameIsValid(projectName);
        assertThat(isValid).isFalse();
    }

    @Test
    public void project_name_too_long() {
        String projectName = "Acme-12_aplhaWichIsTooLong";
        boolean isValid = redisProjectStore.projectNameIsValid(projectName);
        assertThat(isValid).isFalse();
    }

}