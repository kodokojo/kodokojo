package io.kodokojo.config.module;

import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.properties.provider.SystemEnvValueProvider;
import io.kodokojo.config.RedisConfig;

public class RedisDockerLinkPropertyValueProvider implements PropertyValueProvider {

    private final PropertyValueProvider delegate;

    private final Integer redisPort;

    private final String redisHost;

    public RedisDockerLinkPropertyValueProvider(PropertyValueProvider delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must be defined.");
        }
        this.delegate = delegate;
        redisPort = delegate.providePropertyValue(Integer.class, "REDIS_PORT_6379_TCP_PORT");
        redisHost = delegate.providePropertyValue(String.class, "REDIS_PORT_6379_TCP_ADDR");
    }

    public RedisDockerLinkPropertyValueProvider() {
        this(new SystemEnvValueProvider());
    }

    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {
        if (redisPort != null) {
            if (RedisConfig.REDIS_HOST.equals(key) && String.class.isAssignableFrom(classType)) {
                return (T) redisHost;
            } else if (RedisConfig.REDIS_PORT.equals(key) && Integer.class.isAssignableFrom(classType)) {
                return (T) redisPort;
            }
        }
        return delegate.providePropertyValue(classType, key);
    }
}


