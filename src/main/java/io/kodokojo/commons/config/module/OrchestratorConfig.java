package io.kodokojo.commons.config.module;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface OrchestratorConfig extends PropertyConfig {

    @Key(value = "orchestrator", defaultValue = "marathon")
    String orchestrator();

}
