package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface ElasticSearchConfig extends PropertyConfig {

    @Key("elasticsearch.url")
    String url();

    @Key(value = "elasticsearch.indexName", defaultValue = "kodokojo")
    String indexName();

}
