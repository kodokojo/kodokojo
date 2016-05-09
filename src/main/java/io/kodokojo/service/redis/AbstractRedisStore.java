package io.kodokojo.service.redis;

import io.kodokojo.brick.BrickFactory;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.apache.commons.lang.StringUtils.isBlank;

public abstract class AbstractRedisStore implements ApplicationLifeCycleListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRedisStore.class);

    protected static final String SALT_KEY;

    static {
        SecureRandom secureRandom = new SecureRandom();
        SALT_KEY = new BigInteger(128, secureRandom).toString(10);
    }

    protected final Key key;

    protected final JedisPool pool;

    protected final MessageDigest messageDigest;

    public AbstractRedisStore(Key key, String host, int port) {
        if (key == null) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        this.key = key;
        pool = createJedisPool(host, port);
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get instance of SHA-1 digest");
        }
    }

    protected abstract String getStoreName();

    protected abstract String getGenerateIdKey();

    protected JedisPool createJedisPool(String host, int port) {
        return new JedisPool(new JedisPoolConfig(), host, port);
    }

    @Override
    public void start() {
        //  Nothing to do
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping {}.", getStoreName());
        if (pool != null) {
            pool.destroy();
        }
    }

    protected String generateId() {
        try (Jedis jedis = pool.getResource()) {
            String id = SALT_KEY + jedis.incr(getGenerateIdKey()).toString();
            String newId = RedisUtils.hexEncode(messageDigest.digest(id.getBytes()));
            return newId;
        }
    }

}
