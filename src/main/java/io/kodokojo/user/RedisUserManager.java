package io.kodokojo.user;

import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.project.model.UserService;
import io.kodokojo.commons.utils.RSAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisUserManager implements UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUserManager.class);

    private final Key key;

    private final JedisPool pool;

    public RedisUserManager(Key key, String url) {
        if (key == null) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (isBlank(url)) {
            throw new IllegalArgumentException("url must be defined.");
        }
        this.key = key;
        pool = createJedisPool(url);
    }

    //  For testing
    protected JedisPool createJedisPool(String url) {
        return new JedisPool(new JedisPoolConfig(), url);
    }

    @Override
    public boolean addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must be defined.");
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArray); Jedis jedis = pool.getResource()) {
            byte[] password = encrypt(user.getPassword());

            UserValue userValue = new UserValue(user, password);
            out.writeObject(userValue);
            jedis.set(user.getUsername().getBytes(), byteArray.toByteArray());
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialized UserValue.", e);
        }
    }


    @Override
    public boolean addUserService(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("userService must be defined.");
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArray); Jedis jedis = pool.getResource()) {
            byte[] password = encrypt(userService.getPassword());
            byte[] privateKey = RSAUtils.wrap(key, userService.getPrivateKey());
            byte[] publicKey = RSAUtils.wrap(key, userService.getPublicKey());

            UserServiceValue userServiceValue = new UserServiceValue(userService, password, privateKey, publicKey);
            out.writeObject(userServiceValue);
            jedis.set(userService.getName().getBytes(), byteArray.toByteArray());
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
        UserValue userValue = (UserValue)  readFromRedis(username.getBytes());
        String password = decrypt(userValue.getPassword());
        return new User(userValue.getName(), userValue.getUsername(), userValue.getEmail(), password, userValue.getSshPublicKey());
    }

    @Override
    public UserService getUserServiceByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        UserServiceValue userServiceValue = (UserServiceValue) readFromRedis(name.getBytes());
        String password = decrypt(userServiceValue.getPassword());
        RSAPrivateKey privateKey = RSAUtils.unwrapPrivateRsaKey(key, userServiceValue.getPrivateKey());
        RSAPublicKey publicKey = RSAUtils.unwrapPublicRsaKey(key, userServiceValue.getPublicKey());
        RSAUtils.unwrap(key, userServiceValue.getPublicKey());
        return new UserService(userServiceValue.getLogin(), userServiceValue.getName(), userServiceValue.getLogin(), password, privateKey, publicKey);
    }

    private Object readFromRedis(byte[] key) {
        try (Jedis jedis = pool.getResource()) {
            byte[] buffer = jedis.get(key);
            ByteArrayInputStream input = new ByteArrayInputStream(buffer);
            try (ObjectInputStream in = new ObjectInputStream(input)) {
                return in.readObject();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create Object input stream", e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unable to found class UserServiceValue ?", e);
            }
        }
    }

    public void stop() {
        if (pool != null) {
            pool.destroy();
        }

    }

    private byte[] encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }

    private String decrypt(byte[] encripted) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encripted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }
}
