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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
