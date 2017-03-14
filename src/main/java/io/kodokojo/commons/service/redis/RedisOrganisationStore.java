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

import io.kodokojo.commons.RSAUtils;
import io.kodokojo.commons.service.repository.store.OrganisationStore;
import io.kodokojo.commons.service.repository.store.OrganisationStoreModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisOrganisationStore extends AbstractRedisStore implements OrganisationStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisOrganisationStore.class);

    private static final String ENTITY_GENERATEID_KEY = "organisationId";

    public static final String ENTITY_PREFIX = "organisation/";
    public static final String ADMINS_KEY = "/admins";
    public static final String USERS_KEY = "/users";
    public static final String ORAGNISATIONS_KEY = "organisations";
    public static final String PROJECT_CONFIGS_KEY = "/projectConfigs";

    @Inject
    public RedisOrganisationStore(Key key, String host, int port, String password) {
        super(key, host, port, password);
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
    public OrganisationStoreModel getOrganisationById(String entityIdentifier) {
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] entityKey = RedisUtils.aggregateKey(ENTITY_PREFIX, entityIdentifier);
            if (jedis.exists(entityKey)) {
                byte[] encrypted = jedis.get(entityKey);
                OrganisationModelRedis organisationModelRedis = (OrganisationModelRedis) RSAUtils.decryptObjectWithAES(key, encrypted);
                String adminsKey = ENTITY_PREFIX + entityIdentifier + ADMINS_KEY;
                String userKey = ENTITY_PREFIX + entityIdentifier + USERS_KEY;
                String projectConfigKey = ENTITY_PREFIX + entityIdentifier + PROJECT_CONFIGS_KEY;
                List<String> users = new ArrayList<>();
                users.addAll(jedis.smembers(userKey));
                List<String> admins = new ArrayList<>();
                admins.addAll(jedis.smembers(adminsKey));
                List<String> projectConfiguration = new ArrayList<>();
                projectConfiguration.addAll(jedis.smembers(projectConfigKey));
                OrganisationStoreModel entity = new OrganisationStoreModel(organisationModelRedis.getIdentifier(), organisationModelRedis.getName(), organisationModelRedis.isConcrete(), projectConfiguration, admins, users);
                return entity;
            }
        }
        return null;
    }


    @Override
    public Set<String> getOrganisationIds() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smembers(ORAGNISATIONS_KEY);

        }
    }


    @Override
    public String addOrganisation(OrganisationStoreModel organisation) {
        if (organisation == null) {
            throw new IllegalArgumentException("organisation must be defined.");
        }
        if (StringUtils.isNotBlank(organisation.getIdentifier())) {
            LOGGER.warn("Try to add organisation {} which have already an ID ({}), we don't insert it in redis.", organisation.getName(), organisation.getIdentifier());
            return organisation.getIdentifier();
        }
        try (Jedis jedis = pool.getResource()) {
            String id = generateId();
            List<String> admins = organisation.getAdmins();
            List<String> users = organisation.getUsers();
            List<String> projectConfigurations = organisation.getProjectConfigurations();

            OrganisationModelRedis organisationModelRedis = new OrganisationModelRedis(organisation);

            //LOGGER.debug("Using key {}", key.getAlgorithm());

            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, organisationModelRedis);
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
            jedis.sadd(ORAGNISATIONS_KEY, id);
            return id;
        }
    }

    @Override
    public void addUserToOrganisation(String userIdentifier, String organisationIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        if (isBlank(organisationIdentifier)) {
            throw new IllegalArgumentException("organisationIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(RedisUtils.aggregateKey(ENTITY_PREFIX, organisationIdentifier))) {
                jedis.sadd(ENTITY_PREFIX + organisationIdentifier + USERS_KEY, userIdentifier);
            }
        }
    }

    @Override
    public void addAdminToOrganisation(String userIdentifier, String organisationIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        if (isBlank(organisationIdentifier)) {
            throw new IllegalArgumentException("organisationIdentifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(RedisUtils.aggregateKey(ENTITY_PREFIX, organisationIdentifier))) {
                jedis.sadd(ENTITY_PREFIX + organisationIdentifier + ADMINS_KEY, userIdentifier);
            }
        }
    }


}
