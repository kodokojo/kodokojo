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
package io.kodokojo.service.redis;

import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.model.*;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.service.store.ProjectStore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.security.Key;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisProjectStore  extends  AbstractRedisStore implements ProjectStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisProjectStore.class);

    public static final String PROJECT_PREFIX = "project/";

    private static final String PROJECTCONFIGURATION_ID_KEY = "projectConfigurationId";

    public static final String PROJECTCONFIGURATION_PREFIX = "projectConfiguration/";

    public static final String PROJECTCONFIG_TO_PROJECT_PREFIX = "projectConfigurationToConfig/";

    public static final String USER_TO_PROJECTCONFIGS_PREFIX = "userToprojectConfigurations/";

    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9\\-_]){4,20}");

    private final BrickFactory brickFactory;

    public RedisProjectStore(Key key, String host, int port, BrickFactory brickFactory) {
        super(key, host, port);
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }
        this.brickFactory = brickFactory;
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
    public String addProjectConfiguration(ProjectConfiguration projectConfiguration) {
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
        return writeProjectConfiguration(new ProjectConfiguration(projectConfiguration.getEntityIdentifier(), identifier, projectConfiguration.getName(), IteratorUtils.toList(projectConfiguration.getAdmins()), projectConfiguration.getStackConfigurations(), IteratorUtils.toList(projectConfiguration.getUsers())));
    }

    @Override
    public void updateProjectConfiguration(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        writeProjectConfiguration(projectConfiguration);
    }

    @Override
    public void setContextToBrickConfiguration(String projectConfigurationId, BrickConfiguration brickConfiguration, Map<String, Serializable> context) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        if (context == null) {
            throw new IllegalArgumentException("context must be defined.");
        }
        ProjectConfiguration projectConfiguration = getProjectConfigurationById(projectConfigurationId);
        BrickConfiguration current = null;
        Iterator<BrickConfiguration> defaultBrickConfigurations = projectConfiguration.getDefaultBrickConfigurations();
        while(defaultBrickConfigurations.hasNext() && current == null) {
            BrickConfiguration tmp = defaultBrickConfigurations.next();
            if (brickConfiguration.getName().equals(tmp.getName()) && brickConfiguration.getType() == tmp.getType()) {
                current = tmp;
            }
        }
        if(current != null) {
            current.setCustomData(context);
            updateProjectConfiguration(projectConfiguration);
        }

    }

    @Override
    public ProjectConfiguration getProjectConfigurationById(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectConfigKey = RedisUtils.aggregateKey(PROJECTCONFIGURATION_PREFIX, identifier);
            if (jedis.exists(projectConfigKey)) {
                byte[] encrypted = jedis.get(projectConfigKey);
                ProjectConfiguration projectConfiguration = (ProjectConfiguration) RSAUtils.decryptObjectWithAES(key, encrypted);
                projectConfiguration.getStackConfigurations().forEach(this::fillStackConfigurationBrick);
                return projectConfiguration;
            }
        }
        return null;
    }

    private void fillStackConfigurationBrick(StackConfiguration stackConfiguration) {
        List<BrickConfiguration> brickConfigurationUpdated = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(stackConfiguration.getBrickConfigurations())) {
            brickConfigurationUpdated.addAll(stackConfiguration.getBrickConfigurations().stream().map(brickConfiguration -> {
                Brick brick = brickFactory.createBrick(brickConfiguration.getName());
                return new BrickConfiguration(brick, brickConfiguration.getName(), brickConfiguration.getType(), brickConfiguration.getUrl(), brick.getVersion(), brickConfiguration.isWaitRunning());
            }).collect(Collectors.toList()));
        }
        stackConfiguration.getBrickConfigurations().clear();
        stackConfiguration.getBrickConfigurations().addAll(brickConfigurationUpdated);
    }

    @Override
    public String addProject(Project project, String projectConfigurationIdentifier) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        if (projectNameIsValid(project.getName())) {
            try (Jedis jedis = pool.getResource()) {
                String identifier = generateId();
                Project toAdd = new Project(identifier, projectConfigurationIdentifier, project.getName(), project.getSslRootCaKey(), project.getSnapshotDate(), project.getStacks());
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
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (isBlank(project.getIdentifier())) {
            throw new IllegalArgumentException("Project identifier() must be defined.");
        }
        try (Jedis jedis= pool.getResource()) {
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


    private String writeProjectConfiguration(ProjectConfiguration projectConfiguration) {
        try (Jedis jedis = pool.getResource()) {
            String identifier = projectConfiguration.getIdentifier();

            ProjectConfiguration toInsert = new ProjectConfiguration(projectConfiguration.getEntityIdentifier(), identifier, projectConfiguration.getName(), IteratorUtils.toList(projectConfiguration.getAdmins()), projectConfiguration.getStackConfigurations(), IteratorUtils.toList(projectConfiguration.getUsers()));
            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, toInsert);
            jedis.set(RedisUtils.aggregateKey(PROJECTCONFIGURATION_PREFIX, identifier), encryptedObject);
            writeUserToProjectConfigurationId(jedis, toInsert.getAdmins(), toInsert.getIdentifier());
            writeUserToProjectConfigurationId(jedis, toInsert.getUsers(), toInsert.getIdentifier());
            return identifier;
        }
    }

    private void writeUserToProjectConfigurationId(Jedis jedis, Iterator<User> users, String projectConfigurationId) {
        byte[] projectConfId = projectConfigurationId.getBytes();
        while(users.hasNext()) {
            User user = users.next();
            byte[] key = RedisUtils.aggregateKey(USER_TO_PROJECTCONFIGS_PREFIX, user.getIdentifier());
            jedis.sadd(key, projectConfId);
        }
    }

}