package io.kodokojo.service.redis;

import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.model.Project;
import io.kodokojo.service.ProjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.Key;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisProjectStore implements ProjectStore, ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisProjectStore.class);

    public static final String PROJECT_PREFIX = "project/";

    public static final String DEFAULT_LB_IP_KEY = "loadbalancerIp";

    public static final String DEFAULT_SSH_PORT = "loadbalancerIp";

    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9\\-_]){0,20}");

    private final Key key;

    private final JedisPool pool;

    public RedisProjectStore(Key key, String host, int port) {
        if (key == null) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        this.key = key;
        pool = createJedisPool(host, port);

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
    public void addProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] encryptedObject = RSAUtils.encryptObjectWithAES(key, project);
            jedis.set(RedisUtils.aggregateKey(PROJECT_PREFIX, project.getName()), encryptedObject);
        }
    }

    @Override
    public Project getProjectByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] projectKey = RedisUtils.aggregateKey(PROJECT_PREFIX, name);
            if (jedis.exists(projectKey)) {
                return (Project) RSAUtils.decryptObjectWithAES(key, jedis.get(projectKey));
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
        LOGGER.info("Stopping RedisBootstrapConfigurationProvider.");
        if (pool != null) {
            pool.destroy();
        }
    }
}