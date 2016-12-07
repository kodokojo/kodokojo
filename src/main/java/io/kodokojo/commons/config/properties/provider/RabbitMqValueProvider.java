package io.kodokojo.commons.config.properties.provider;

import io.kodokojo.commons.config.MicroServiceConfig;

import static java.util.Objects.requireNonNull;

public class RabbitMqValueProvider implements PropertyValueProvider {

    private final MicroServiceConfig microServiceConfig;

    private final PropertyValueProvider delagte;

    public RabbitMqValueProvider(MicroServiceConfig microServiceConfig, PropertyValueProvider delagte) {
        requireNonNull(microServiceConfig, "microServiceConfig must be defined.");
        requireNonNull(delagte, "delagte must be defined.");
        this.microServiceConfig = microServiceConfig;
        this.delagte = new RabbitMqDockerLinkPropertyValueProvider(delagte);
    }

    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {
        if (String.class.isAssignableFrom(classType) && "rabbitmq.service".equals(key)) {
            return (T) microServiceConfig.name();
        }
        return delagte.providePropertyValue(classType, key);
    }
}
