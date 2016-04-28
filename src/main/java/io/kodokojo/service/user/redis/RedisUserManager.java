package io.kodokojo.service.user.redis;

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

import io.kodokojo.service.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.model.User;
import io.kodokojo.model.UserService;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.service.redis.RedisUtils;
import io.kodokojo.service.UserManager;
import io.kodokojo.service.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisUserManager implements UserManager, ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUserManager.class);

    private static final String ID_KEY = "kodokojo-userId";

    private static final String SALT_KEY = "kodokojo-salt-key";

    private static final byte[] NEW_USER_CONTENT = new byte[]{0, 1, 1, 0};

    private static final int DEFAULT_NEW_ID_TTL = 5 * 60; //5 minutes

    public static final String NEW_ID_PREFIX = "newId/";

    public static final String USER_PREFIX = "user/";

    public static final String USERNAME_PREFIX = "usenamer/";

    public static final String USERSERVICE_PREFIX = "userservice/";

    public static final String USERSERVICENAME_PREFIX = "userservicename/";

    private final Key key;

    private final JedisPool pool;

    private final String salt;

    private final MessageDigest messageDigest;

    private final int newIdExpirationTime;

    public RedisUserManager(Key key, String host, int port, int newIdExpirationTime) {
        if (key == null) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }

        this.key = key;
        pool = createJedisPool(host, port);

        try (Jedis jedis = pool.getResource()) {
            String salt = jedis.get(SALT_KEY);
            if (isBlank(salt)) {
                try {
                    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                    this.salt = new BigInteger(130, secureRandom).toString(32);
                    jedis.set(SALT_KEY, this.salt);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Unable to get a valid SecureRandom instance", e);
                }
            } else {
                this.salt = salt;
            }
        }

        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get instance of SHA-1 digest");
        }
        this.newIdExpirationTime = newIdExpirationTime;
    }


    public RedisUserManager(Key key, String host, int port) {
        this(key, host, port, DEFAULT_NEW_ID_TTL);
    }

    //  For testing
    protected JedisPool createJedisPool(String host, int port) {
        return new JedisPool(new JedisPoolConfig(), host, port);
    }

    @Override
    public String generateId() {
        try (Jedis jedis = pool.getResource()) {
            String id = salt + jedis.incr(ID_KEY).toString();
            String newId = RedisUtils.hexEncode(messageDigest.digest(id.getBytes()));
            byte[] prefixedKey = RedisUtils.aggregateKey(NEW_ID_PREFIX, newId);
            jedis.set(prefixedKey, NEW_USER_CONTENT);
            jedis.expire(prefixedKey, newIdExpirationTime);
            return newId;
        }
    }

    @Override
    public boolean identifierExpectedNewUser(String generatedId) {
        if (isBlank(generatedId)) {
            throw new IllegalArgumentException("generatedId must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(RedisUtils.aggregateKey(NEW_ID_PREFIX, generatedId));
        }
    }

    @Override
    public boolean addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must be defined.");
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArray); Jedis jedis = pool.getResource()) {
            if (jedis.get(RedisUtils.aggregateKey(USERNAME_PREFIX, user.getUsername())) == null) {
                byte[] previous = jedis.get(RedisUtils.aggregateKey(NEW_ID_PREFIX, user.getIdentifier()));
                if (Arrays.equals(previous, NEW_USER_CONTENT) &&
                        !jedis.exists(RedisUtils.aggregateKey(USER_PREFIX, user.getIdentifier()))) {
                    byte[] password = RSAUtils.encryptWithAES(key,user.getPassword());

                    UserValue userValue = new UserValue(user, password);
                    out.writeObject(userValue);

                    jedis.set(RedisUtils.aggregateKey(USER_PREFIX, user.getIdentifier()), byteArray.toByteArray());
                    jedis.set(USERNAME_PREFIX + user.getUsername(), user.getIdentifier());
                    jedis.del((NEW_ID_PREFIX + user.getIdentifier()).getBytes());
                    return true;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialized UserValue.", e);
        }
        return false;
    }


    @Override
    public boolean addUserService(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("userService must be defined.");
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArray); Jedis jedis = pool.getResource()) {
            byte[] password = RSAUtils.encryptWithAES(key, userService.getPassword());
            byte[] privateKey = RSAUtils.wrap(key, userService.getPrivateKey());
            byte[] publicKey = RSAUtils.wrap(key, userService.getPublicKey());

            UserServiceValue userServiceValue = new UserServiceValue(userService, password, privateKey, publicKey);
            out.writeObject(userServiceValue);
            jedis.set(RedisUtils.aggregateKey(USERSERVICE_PREFIX, userService.getIdentifier()), byteArray.toByteArray());
            jedis.set(USERSERVICENAME_PREFIX + userService.getName(), userService.getIdentifier());
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialized UserValue.", e);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            String identifier = jedis.get(USERNAME_PREFIX + username);
            if (isBlank(identifier)) {
                return null;
            }
            return getUserByIdentifier(identifier);
        }
    }

    @Override
    public UserService getUserServiceByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            String identifier = jedis.get(USERSERVICENAME_PREFIX + name);
            if (isBlank(identifier)) {
                return null;
            }
            UserServiceValue userServiceValue = (UserServiceValue) RedisUtils.readFromRedis(pool, RedisUtils.aggregateKey(USERSERVICE_PREFIX, identifier));
            if (userServiceValue == null) {
                return null;
            }
            String password = RSAUtils.decryptWithAES(key, userServiceValue.getPassword());
            RSAPrivateKey privateKey = RSAUtils.unwrapPrivateRsaKey(key, userServiceValue.getPrivateKey());
            RSAPublicKey publicKey = RSAUtils.unwrapPublicRsaKey(key, userServiceValue.getPublicKey());
            RSAUtils.unwrap(key, userServiceValue.getPublicKey());
            return new UserService(userServiceValue.getLogin(), userServiceValue.getName(), userServiceValue.getLogin(), password, privateKey, publicKey);
        }
    }

    @Override
    public User getUserByIdentifier(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        UserValue userValue = (UserValue) RedisUtils.readFromRedis(pool, RedisUtils.aggregateKey(USER_PREFIX, identifier));
        if (userValue == null) {
            return null;
        }
        String password = RSAUtils.decryptWithAES(key, userValue.getPassword());
        return new User(identifier, userValue.getName(), userValue.getUsername(), userValue.getEmail(), password, userValue.getSshPublicKey());
    }


    @Override
    public void start() {
        // Nothing to do
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping RedisUserManager.");
        if (pool != null) {
            pool.destroy();
        }
    }



}
