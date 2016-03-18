package io.kodokojo.service.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class RedisUtils {

    private RedisUtils() {
        //
    }

    public static Object readFromRedis(JedisPool pool, byte[] key) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) {
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
        return null;
    }


    public static byte[] aggregateKey(String prefix, String key) {
        assert isNotBlank(prefix) : "prefix must be defined";
        assert isNotBlank(key) : "key must be defined";
        return (prefix + key).getBytes();
    }

}
