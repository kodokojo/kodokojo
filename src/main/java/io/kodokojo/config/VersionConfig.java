package io.kodokojo.config;

import io.kodokojo.config.properties.Key;
import io.kodokojo.config.properties.PropertyConfig;

public interface VersionConfig extends PropertyConfig {

    @Key("version")
    String version();

    @Key("gitSha1")
    String gitSha1();

    @Key("branch")
    String branch();

}
