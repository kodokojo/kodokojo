package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

public interface SecurityConfig extends PropertyConfig {

    @Key(value = "security.secretkey.path", defaultValue = "secret.key")
    String privateKeyPath();

    @Key("security.ssl.rootCa.pemPath")
    String sslRootCaPemPath();

    @Key("security.ssl.rootCa.ks.alias")
    String sslRootCaKsAlias();

    @Key("security.ssl.rootCa.ks.password")
    String sslRootCaKsPassword();

}
