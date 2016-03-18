package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface MarathonConfig extends PropertyConfig {

    @Key("marathon.url")
    String url();

}
