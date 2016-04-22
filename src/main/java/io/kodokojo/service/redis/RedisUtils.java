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
        // Utility class.
    }

    /**
     * Read a serialized Object in a Redis value
     * @param pool Jedis pool to connect to Redis
     * @param key The key where find the expected Object
     * @return The serialized Object.
     */
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
                    throw new IllegalStateException("Unable to found class Object ?", e);
                }
            }
        }
        return null;
    }

    /**
     * Aggregate to Sring and return byte Array. Usefull to create a Redis key.
     * @param prefix The key prefix
     * @param key The key
     * @return An aggregate key
     */
    public static byte[] aggregateKey(String prefix, String key) {
        assert isNotBlank(prefix) : "prefix must be defined";
        assert isNotBlank(key) : "key must be defined";
        return (prefix + key).getBytes();
    }

    public static String hexEncode(byte[] aInput) {
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(digits[(b & 0xf0) >> 4]);
            result.append(digits[b & 0x0f]);
        }
        return result.toString();
    }

}
