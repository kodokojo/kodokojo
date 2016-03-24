package io.kodokojo.service.redis;

import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.service.ProjectStore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisProjectStore implements ProjectStore, ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisProjectStore.class);

    public static final String PROJECT_PREFIX = "project/";

    private static final String SALT_KEY;

    private static final String PROJECTCONFIGURATION_ID_KEY = "projectConfigurationId";

    public static final String PROJECTCONFIGURATION_PREFIX = "projectConfiguration/";

    public static final String DEFAULT_LB_IP_KEY = "loadbalancerIp";

    public static final String DEFAULT_SSH_PORT = "loadbalancerIp";

    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9\\-_]){0,20}");

    static {
        SecureRandom secureRandom = new SecureRandom();
        SALT_KEY = new BigInteger(128, secureRandom).toString(10);
    }

    private final Key key;

    private final JedisPool pool;

    private final MessageDigest messageDigest;

    public RedisProjectStore(Key key, String host, int port) {
        if (key == null) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        this.key = key;
        pool = createJedisPool(host, port);
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get instance of SHA-1 digest");
        }
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
        if (StringUtils.isNotBlank(projectConfiguration.getIdentifier())) {
            throw new IllegalArgumentException("ProjectConfiguration " + projectConfiguration.getName() + " already exist");
        }
        try (Jedis jedis = pool.getResource()) {
            String identifier = generateId();
            Date versionDate = new Date();
            ProjectConfiguration toInsert = new ProjectConfiguration(identifier, projectConfiguration.getName(), projectConfiguration.getOwnerEmail(), projectConfiguration.getStackConfigurations(), projectConfiguration.getUsers());
            toInsert.setVersionDate(versionDate);
            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, toInsert);
            jedis.set(RedisUtils.aggregateKey(PROJECTCONFIGURATION_PREFIX, identifier), encryptedObject);
            return identifier;
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
                return (ProjectConfiguration) RSAUtils.decryptObjectWithAES(key, encrypted);
            }
        }
        return null;
    }

    @Override
    public String addProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (projectNameIsValid(project.getName())) {
            try (Jedis jedis = pool.getResource()) {
                String identifier = generateId();
                Project toAdd = new Project(identifier, project.getName(), project.getSslRootCaKey(), project.getSnapshotDate(), project.getStacks());
                byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, toAdd);
                jedis.set(RedisUtils.aggregateKey(PROJECT_PREFIX, project.getName()), encryptedObject);
                return identifier;
            }
        }
        return null;
    }

    @Override
    public Project getProjectByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectKey = RedisUtils.aggregateKey(PROJECT_PREFIX, name);
            if (jedis.exists(projectKey)) {
                byte[] encrypted = jedis.get(projectKey);
                return (Project) RSAUtils.decryptObjectWithAES(key, encrypted);
            }
        }
        return null;
    }

    protected JedisPool createJedisPool(String host, int port) {
        return new JedisPool(new JedisPoolConfig(), host, port);
    }

    @Override
    public void start() {
        //  Nothing to do
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping RedisProjectStore.");
        if (pool != null) {
            pool.destroy();
        }
    }

    private String generateId() {
        try (Jedis jedis = pool.getResource()) {
            String id = SALT_KEY + jedis.incr(PROJECTCONFIGURATION_ID_KEY).toString();
            String newId = RedisUtils.hexEncode(messageDigest.digest(id.getBytes()));
            return newId;
        }
    }
}