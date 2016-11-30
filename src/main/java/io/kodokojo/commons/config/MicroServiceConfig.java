package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.PropertyConfig;

public interface MicroServiceConfig extends PropertyConfig{

    String name();

    String uuid();
}
