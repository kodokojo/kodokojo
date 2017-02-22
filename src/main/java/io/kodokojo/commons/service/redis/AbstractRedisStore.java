/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.service.redis;

import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.apache.commons.lang.StringUtils.isBlank;

public abstract class AbstractRedisStore implements ApplicationLifeCycleListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRedisStore.class);

    protected final String saltKey;

    protected final Key key;

    protected final JedisPool pool;

    protected final MessageDigest messageDigest;

    public AbstractRedisStore(Key key, String host, int port, String password) {
        if (key == null) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        this.key = key;
        pool = createJedisPool(host, port, password);
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get instance of SHA-1 digest");
        }

        SecureRandom secureRandom = new SecureRandom();
        saltKey = new BigInteger(128, secureRandom).toString(10);
    }

    protected abstract String getStoreName();

    protected abstract String getGenerateIdKey();

    protected JedisPool createJedisPool(String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);

        return new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password);
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

            SecureRandom secureRandom = new SecureRandom();
            String rand = new BigInteger(128, secureRandom).toString(10);
            String id = saltKey + rand +  jedis.incr(getGenerateIdKey()).toString();
            String newId = RedisUtils.hexEncode(messageDigest.digest(id.getBytes()));
            return newId;
        }
    }

}
