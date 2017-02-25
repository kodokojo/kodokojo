package io.kodokojo.commons.service.healthcheck;

import io.kodokojo.commons.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class RedisHealthChecker implements HealthChecker {

    private static final String PONG = "PONG";

    private static final String HEALTHCHECKER_NAME = "Redis";

    private final RedisConfig redisConfig;

    @Inject
    public RedisHealthChecker(RedisConfig redisConfig) {
        requireNonNull(redisConfig, "redisConfig must be defined.");
        this.redisConfig = redisConfig;
    }

    @Override
    public HealthCheck check() {
        HealthCheck.Builder builder = new HealthCheck.Builder();
        builder.setName(HEALTHCHECKER_NAME);
        try (Jedis jedis = createJedisPool().getResource()) {
            String pong = jedis.ping();
            builder.setState(PONG.equals(pong) ? HealthCheck.State.OK : HealthCheck.State.FAIL);
        } catch (RuntimeException e) {
            builder.setState(HealthCheck.State.FAIL);
            builder.setDetail(e.getMessage());
        }
        return builder.build();
    }

    protected JedisPool createJedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        return new JedisPool(config, redisConfig.host(), redisConfig.port());
    }
}
