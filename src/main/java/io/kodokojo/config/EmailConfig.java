package io.kodokojo.config;

import io.kodokojo.commons.utils.properties.Key;

public interface EmailConfig extends ApplicationConfig {

    @Key(value = "smtp.host")
    String smtpHost();

    @Key(value = "smtp.port", defaultValue = "587")
    int smtpPort();

    @Key(value = "smtp.username")
    String smtpUsername();

    @Key(value = "smtp.password")
    String smtpPassword();

}
