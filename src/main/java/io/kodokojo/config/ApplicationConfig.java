package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;
import io.kodokojo.commons.utils.properties.PropertyConfig;

import java.util.concurrent.TimeUnit;

public interface ApplicationConfig extends PropertyConfig {

    @Key(value = "application.port", defaultValue = "80")
    int port();

    @Key("application.dns.domain")
    String domain();

    @Key("lb.defaultIp")
    String defaultLoadbalancerIp();

    @Key(value = "initSshport", defaultValue = "32768")
    int initialSshPort();

    @Key(value = "ssl.ca.duration", defaultValue = "8035200000") //3 mouths
    long sslCaDuration();
}
