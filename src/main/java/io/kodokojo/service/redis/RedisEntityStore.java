package io.kodokojo.service.redis;

import io.kodokojo.brick.BrickFactory;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.model.Entity;
import io.kodokojo.model.User;
import io.kodokojo.service.store.EntityStore;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisEntityStore extends AbstractRedisStore implements EntityStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisEntityStore.class);

    private static final String ENTITY_GENERATEID_KEY = "entityId";

    public static final String ENTITY_PREFIX = "entity/";

    public static final String ENTITY_USER_PREFIX = "entityUsers/";

    @Inject
    public RedisEntityStore(Key key, String host, int port) {
        super(key, host, port);
    }

    @Override
    protected String getStoreName() {
        return "RedisEntityStore";
    }

    @Override
    protected String getGenerateIdKey() {
        return ENTITY_GENERATEID_KEY;
    }

    @Override
    public String addEntity(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must be defined.");
        }
        if (StringUtils.isNotBlank(entity.getIdentifier())) {
            throw  new IllegalArgumentException("entity had already an id.");
        }
        try (Jedis jedis = pool.getResource()) {
            String id = generateId();
            List<User> admins = IteratorUtils.toList(entity.getAdmins());
            List<User> users = IteratorUtils.toList(entity.getUsers());
            Entity entityToWrite = new Entity(id, entity.getName(), entity.isConcrete(),
                    IteratorUtils.toList(entity.getProjectConfigurations()),
                    admins,
                    users);

            List<User> allUsers = new ArrayList<>(users);
            allUsers.addAll(admins);
            Set<String> userIds = allUsers.stream().map(User::getIdentifier).collect(Collectors.toSet());

            userIds.stream().forEach(userId -> {
                jedis.set(RedisUtils.aggregateKey(ENTITY_USER_PREFIX, userId), id.getBytes());
            });

            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, entityToWrite);
            jedis.set(RedisUtils.aggregateKey(ENTITY_PREFIX, id), encryptedObject);
            return id;
        }
    }

    @Override
    public Entity getEntityById(String entityIdentifier) {
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] entityKey = RedisUtils.aggregateKey(ENTITY_PREFIX, entityIdentifier);
            if (jedis.exists(entityKey)) {
                byte[] encrypted = jedis.get(entityKey);
                Entity entity = (Entity) RSAUtils.decryptObjectWithAES(key, encrypted);
                return entity;
            }
        }
        return null;
    }

    @Override
    public String getEntityOfUserId(String userIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] key = RedisUtils.aggregateKey(ENTITY_USER_PREFIX, userIdentifier);
            if (jedis.exists(key)) {
                return new String(jedis.get(key));
            }
        }
        return null;
    }
}
