package io.kodokojo.bdd;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface MarathonConfig extends PropertyConfig {

    @Key("marathon.host")
    String marathonHost();

    @Key(value = "marathon.port", defaultValue = "8080")
    int marathonPort();

}
