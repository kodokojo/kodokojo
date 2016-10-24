package io.kodokojo.config;

import io.kodokojo.config.properties.Key;
import io.kodokojo.config.properties.PropertyConfig;

public interface ZookeeperConfig extends PropertyConfig {

    @Key("zookeeper.url")
    String url();

}
