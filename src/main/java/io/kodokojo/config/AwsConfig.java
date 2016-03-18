package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface AwsConfig extends PropertyConfig {

    @Key(value = "aws.region", defaultValue = "eu-west-1")
    String region();

}
