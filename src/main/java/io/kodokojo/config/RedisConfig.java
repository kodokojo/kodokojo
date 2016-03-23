package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface RedisConfig extends PropertyConfig {

    String REDIS_HOST = "redis.host";

    String REDIS_PORT = "redis.port";

    @Key(value = REDIS_HOST, defaultValue = "redis")
    String host();

    @Key(value = REDIS_PORT, defaultValue = "6379")
    Integer port();

}
