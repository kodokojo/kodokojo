package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface SecurityConfig extends PropertyConfig {

    @Key(value = "security.key.path", defaultValue = "secret.key")
    String privateKeyPath();

}
