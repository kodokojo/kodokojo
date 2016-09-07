package io.kodokojo.config;

import io.kodokojo.config.properties.Key;
import io.kodokojo.config.properties.PropertyConfig;

public interface ReCaptchaConfig extends PropertyConfig {

    @Key("recaptcha.secret")
    String secret();

}
