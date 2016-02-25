package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface RedisConfig extends PropertyConfig {

    @Key("redis.host")
    String host();

    @Key("redis.port")
    int port();

}
