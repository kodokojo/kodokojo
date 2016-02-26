package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface ApplicationConfig extends PropertyConfig {

    @Key(value = "application.port", defaultValue = "8080")
    int port();

}
