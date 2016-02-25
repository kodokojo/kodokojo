package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface SecurityConfig extends PropertyConfig {

    @Key("security.key.path")
    String privateKeyPath();

}
