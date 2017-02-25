package io.kodokojo.commons.service.healthcheck;

import io.kodokojo.commons.config.RedisConfig;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RedisHealthCheckerTest {

    RedisHealthChecker redisHealthChecker;

    JedisPool jedisPoolMocked;

    Jedis jedisMocked;

    @Before
    public void setup() {
        RedisConfig redisConfiguration = mock(RedisConfig.class);
        jedisPoolMocked = mock(JedisPool.class);
        jedisMocked = mock(Jedis.class);
        redisHealthChecker = new RedisHealthChecker(redisConfiguration) {
            @Override
            protected JedisPool createJedisPool() {
                return jedisPoolMocked;
            }
        };
    }

    @Test
    public void valid_pong() {

        when(jedisPoolMocked.getResource()).thenReturn(jedisMocked);
        when(jedisMocked.ping()).thenReturn("PONG");

        HealthCheck healthCheck = redisHealthChecker.check();

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getState()).isEqualTo(HealthCheck.State.OK);

    }

    @Test
    public void valid_ping_response_not_expected() {

        when(jedisPoolMocked.getResource()).thenReturn(jedisMocked);
        when(jedisMocked.ping()).thenReturn("WTF");

        HealthCheck healthCheck = redisHealthChecker.check();

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getState()).isEqualTo(HealthCheck.State.FAIL);

    }


    @Test
    public void ping_throw_exception() {

        when(jedisPoolMocked.getResource()).thenReturn(jedisMocked);
        when(jedisMocked.ping()).thenThrow(new RuntimeException("fake fail exception"));

        HealthCheck healthCheck = redisHealthChecker.check();

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getState()).isEqualTo(HealthCheck.State.FAIL);

    }

}