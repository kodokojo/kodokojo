/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.redis;

import io.kodokojo.model.Project;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.service.repository.store.ProjectConfigurationStoreModel;
import io.kodokojo.service.repository.store.ProjectStore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.security.Key;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisProjectStore extends AbstractRedisStore implements ProjectStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisProjectStore.class);

    public static final String PROJECT_PREFIX = "project/";

    private static final String PROJECTCONFIGURATION_ID_KEY = "projectConfigurationId";

    public static final String PROJECTCONFIGURATION_PREFIX = "projectConfiguration/";

    public static final String PROJECTCONFIG_TO_PROJECT_PREFIX = "projectConfigurationToConfig/";

    public static final String USER_TO_PROJECTCONFIGS_PREFIX = "userToprojectConfigurations/";

    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9\\-_]){4,20}");


    public RedisProjectStore(Key key, String host, int port) {
        super(key, host, port);
    }

    @Override
    protected String getStoreName() {
        return "ProjectRedisStore";
    }

    @Override
    protected String getGenerateIdKey() {
        return PROJECTCONFIGURATION_ID_KEY;
    }

    @Override
    public boolean projectNameIsValid(String projectName) {
        if (isBlank(projectName)) {
            return false;
        }
        Matcher matcher = PROJECT_NAME_PATTERN.matcher(projectName);
        if (matcher.matches()) {
            try (Jedis jedis = pool.getResource()) {
                byte[] key = RedisUtils.aggregateKey(PROJECT_PREFIX, projectName);
                return !jedis.exists(key);
            }
        }
        return false;
    }

    @Override
    public String addProjectConfiguration(ProjectConfigurationStoreModel projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (isBlank(projectConfiguration.getEntityIdentifier())) {
            throw new IllegalArgumentException("EntityIdentifier must be defined.");
        }
        if (StringUtils.isNotBlank(projectConfiguration.getIdentifier())) {
            throw new IllegalArgumentException("ProjectConfiguration " + projectConfiguration.getName() + " already exist");
        }
        String identifier = generateId();
        return writeProjectConfiguration(new ProjectConfigurationStoreModel(projectConfiguration.getEntityIdentifier(), identifier, projectConfiguration.getName(), projectConfiguration.getUserService(), projectConfiguration.getAdmins(), projectConfiguration.getStackConfigurations(), projectConfiguration.getUsers()));
    }

    @Override
    public void updateProjectConfiguration(ProjectConfigurationStoreModel projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        writeProjectConfiguration(projectConfiguration);
    }

    @Override
    public ProjectConfigurationStoreModel getProjectConfigurationById(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectConfigKey = RedisUtils.aggregateKey(PROJECTCONFIGURATION_PREFIX, identifier);
            if (jedis.exists(projectConfigKey)) {
                byte[] encrypted = jedis.get(projectConfigKey);
                ProjectConfigurationStoreModel projectConfiguration = (ProjectConfigurationStoreModel) RSAUtils.decryptObjectWithAES(key, encrypted);
                return projectConfiguration;
            }
        }
        return null;
    }


    @Override
    public String addProject(Project project, String projectConfigurationIdentifier) {
        requireNonNull(project, "project must be defined.");
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        if (projectNameIsValid(project.getName())) {
            try (Jedis jedis = pool.getResource()) {
                String identifier = generateId();
                Project toAdd = new Project(identifier, projectConfigurationIdentifier, project.getName(), project.getSnapshotDate(), project.getStacks());
                byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, toAdd);
                jedis.set(RedisUtils.aggregateKey(PROJECT_PREFIX, identifier), encryptedObject);
                jedis.set(RedisUtils.aggregateKey(PROJECTCONFIG_TO_PROJECT_PREFIX, projectConfigurationIdentifier), identifier.getBytes());
                return identifier;
            }
        }
        return null;
    }

    @Override
    public void updateProject(Project project) {
        requireNonNull(project, "project must be defined.");
        if (isBlank(project.getIdentifier())) {
            throw new IllegalArgumentException("Project identifier() must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, project);
            jedis.set(RedisUtils.aggregateKey(PROJECT_PREFIX, project.getIdentifier()), encryptedObject);
        }
    }

    @Override
    public Set<String> getProjectConfigIdsByUserIdentifier(String userIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        Set<String> res = new HashSet<>();
        try (Jedis jedis = pool.getResource()) {
            byte[] projectConfigKey = RedisUtils.aggregateKey(USER_TO_PROJECTCONFIGS_PREFIX, userIdentifier);
            if (jedis.exists(projectConfigKey)) {
                res.addAll(jedis.smembers(projectConfigKey).stream().map(String::new).collect(Collectors.toSet()));
            }
        }
        return res;
    }

    @Override
    public String getProjectIdByProjectConfigurationId(String projectConfigurationId) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectConfigKey = RedisUtils.aggregateKey(PROJECTCONFIG_TO_PROJECT_PREFIX, projectConfigurationId);
            if (jedis.exists(projectConfigKey)) {
                return new String(jedis.get(projectConfigKey));
            }
        }
        return null;
    }

    @Override
    public Project getProjectByProjectConfigurationId(String projectConfigurationId) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectConfigKey = RedisUtils.aggregateKey(PROJECTCONFIG_TO_PROJECT_PREFIX, projectConfigurationId);
            if (jedis.exists(projectConfigKey)) {
                String projectId = new String(jedis.get(projectConfigKey));
                return getProjectByIdentifier(projectId);
            }
        }
        return null;
    }

    @Override
    public Project getProjectByIdentifier(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectKey = RedisUtils.aggregateKey(PROJECT_PREFIX, identifier);
            if (jedis.exists(projectKey)) {
                byte[] encrypted = jedis.get(projectKey);
                return (Project) RSAUtils.decryptObjectWithAES(key, encrypted);
            }
        }
        return null;
    }


    private String writeProjectConfiguration(ProjectConfigurationStoreModel projectConfiguration) {
        try (Jedis jedis = pool.getResource()) {
            String identifier = projectConfiguration.getIdentifier();

            ProjectConfigurationStoreModel toInsert = new ProjectConfigurationStoreModel(projectConfiguration.getEntityIdentifier(), identifier, projectConfiguration.getName(), projectConfiguration.getUserService(), projectConfiguration.getAdmins(), projectConfiguration.getStackConfigurations(), projectConfiguration.getUsers());
            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, toInsert);
            jedis.set(RedisUtils.aggregateKey(PROJECTCONFIGURATION_PREFIX, identifier), encryptedObject);
            writeUserToProjectConfigurationId(jedis, toInsert.getAdmins(), toInsert.getIdentifier());
            writeUserToProjectConfigurationId(jedis, toInsert.getUsers(), toInsert.getIdentifier());
            return identifier;
        }
    }

    private void writeUserToProjectConfigurationId(Jedis jedis, List<String> users, String projectConfigurationId) {
        byte[] projectConfId = projectConfigurationId.getBytes();
        Iterator<String> it = users.iterator();
        while (it.hasNext()) {
            String user = it.next();
            byte[] key = RedisUtils.aggregateKey(USER_TO_PROJECTCONFIGS_PREFIX, user);
            jedis.sadd(key, projectConfId);
        }
    }

}