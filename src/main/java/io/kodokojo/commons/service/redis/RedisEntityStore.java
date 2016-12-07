/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
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

import io.kodokojo.commons.RSAUtils;
import io.kodokojo.commons.service.repository.store.EntityStore;
import io.kodokojo.commons.service.repository.store.EntityStoreModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisEntityStore extends AbstractRedisStore implements EntityStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisEntityStore.class);

    private static final String ENTITY_GENERATEID_KEY = "entityId";

    public static final String ENTITY_PREFIX = "entity/";
    public static final String ADMINS_KEY = "/admins";
    public static final String USERS_KEY = "/users";
    public static final String PROJECT_CONFIGS_KEY = "/projectConfigs";

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
    public EntityStoreModel getEntityById(String entityIdentifier) {
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] entityKey = RedisUtils.aggregateKey(ENTITY_PREFIX, entityIdentifier);
            if (jedis.exists(entityKey)) {
                byte[] encrypted = jedis.get(entityKey);
                EntityModelRedis entityModelRedis = (EntityModelRedis) RSAUtils.decryptObjectWithAES(key, encrypted);
                String adminsKey = ENTITY_PREFIX + entityIdentifier + ADMINS_KEY;
                String userKey = ENTITY_PREFIX + entityIdentifier + USERS_KEY;
                String projectConfigKey = ENTITY_PREFIX + entityIdentifier + PROJECT_CONFIGS_KEY;
                List<String> users = new ArrayList<>();
                users.addAll(jedis.smembers(userKey));
                List<String> admins = new ArrayList<>();
                admins.addAll(jedis.smembers(adminsKey));
                List<String> projectConfiguration = new ArrayList<>();
                projectConfiguration.addAll(jedis.smembers(projectConfigKey));
                EntityStoreModel entity = new EntityStoreModel(entityModelRedis.getIdentifier(), entityModelRedis.getName(), entityModelRedis.isConcrete(), projectConfiguration, admins, users);
                return entity;
            }
        }
        return null;
    }

    @Override
    public String addEntity(EntityStoreModel entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must be defined.");
        }
        if (StringUtils.isNotBlank(entity.getIdentifier())) {
            throw new IllegalArgumentException("entity had already an id.");
        }
        try (Jedis jedis = pool.getResource()) {
            String id = generateId();
            List<String> admins = entity.getAdmins();
            List<String> users = entity.getUsers();
            List<String> projectConfigurations = entity.getProjectConfigurations();

            EntityModelRedis entityModelRedis = new EntityModelRedis(entity);

            //LOGGER.debug("Using key {}", key.getAlgorithm());

            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, entityModelRedis);
            jedis.set(RedisUtils.aggregateKey(ENTITY_PREFIX, id), encryptedObject);
            if (CollectionUtils.isNotEmpty(admins)) {
                jedis.sadd(ENTITY_PREFIX + id + ADMINS_KEY, admins.toArray(new String[admins.size()]));
            }
            if (CollectionUtils.isNotEmpty(users)) {
                jedis.sadd(ENTITY_PREFIX + id + USERS_KEY, users.toArray(new String[users.size()]));
            }
            if (CollectionUtils.isNotEmpty(projectConfigurations)) {
                jedis.sadd(ENTITY_PREFIX + id + PROJECT_CONFIGS_KEY, projectConfigurations.toArray(new String[projectConfigurations.size()]));
            }
            return id;
        }
    }

    @Override
    public void addUserToEntity(String userIdentifier, String entityIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(RedisUtils.aggregateKey(ENTITY_PREFIX, entityIdentifier))) {
                jedis.sadd(ENTITY_PREFIX + entityIdentifier + USERS_KEY, userIdentifier);
            }
        }
    }


}
