package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface MicroServiceConfig extends PropertyConfig {

    @Key("microservice.name")
    String name();

    @Key(value = "microservice.uuid")
    String uuid();
}
