package io.kodokojo.commons.config.properties.provider;

import io.kodokojo.commons.config.MicroServiceConfig;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class MicroServiceValueProvider implements PropertyValueProvider {

    private final PropertyValueProvider delagte;

    private final String uuid;

    public MicroServiceValueProvider(PropertyValueProvider delagte) {
        requireNonNull(delagte, "delagte must be defined.");
        uuid = UUID.randomUUID().toString();
        this.delagte = delagte;
    }

    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {
        T res = delagte.providePropertyValue(classType, key);
        if (res == null && String.class.isAssignableFrom(classType) && "microservice.uuid".equals(key)) {
            res = (T) uuid;
        }
        return res;
    }
}
