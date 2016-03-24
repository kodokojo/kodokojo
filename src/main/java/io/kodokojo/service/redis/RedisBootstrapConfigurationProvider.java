package io.kodokojo.service.redis;

import io.kodokojo.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.service.BootstrapConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.Key;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RedisBootstrapConfigurationProvider implements BootstrapConfigurationProvider, ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisBootstrapConfigurationProvider.class);


    public static final String DEFAULT_LB_IP_KEY = "loadbalancerIp";

    public static final String DEFAULT_SSH_PORT = "sshPort";

    private final JedisPool pool;

    public RedisBootstrapConfigurationProvider(String host, int port, String defaultLbIp, int initSshPort) {

        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        if (isBlank(defaultLbIp)) {
            throw new IllegalArgumentException("defaultLbIp must be defined.");
        }
        pool = createJedisPool(host, port);

        try (Jedis jedis = pool.getResource()) {
            if (!jedis.exists(DEFAULT_LB_IP_KEY)) {
                jedis.set(DEFAULT_LB_IP_KEY, defaultLbIp);
            }
            if (!jedis.exists(DEFAULT_SSH_PORT)) {
                jedis.set(DEFAULT_SSH_PORT, "" + initSshPort);
            }
        }
    }

    @Override
    public String provideLoadBalancerIp(String projectName, String stackName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        String lbKey = RedisProjectStore.PROJECT_PREFIX + projectName + "/" + stackName + "/lbIp";
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(lbKey)) {
                return jedis.get(lbKey);
            } else {
                return jedis.get(DEFAULT_LB_IP_KEY);
            }
        }
    }

    @Override
    public int provideSshPortEntrypoint(String projectName, String stackName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        String sshPortKey = RedisProjectStore.PROJECT_PREFIX + projectName + "/" + stackName + "/sshPort";
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(sshPortKey)) {
                return Integer.parseInt(jedis.get(sshPortKey));
            } else {
                return jedis.incr(DEFAULT_SSH_PORT).intValue();
            }
        }
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
