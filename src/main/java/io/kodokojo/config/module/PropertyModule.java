package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.KodokojoConfig;
import io.kodokojo.commons.utils.properties.PropertyConfig;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.*;
import io.kodokojo.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class PropertyModule extends AbstractModule {

    public static final String APPLICATION_CONFIGURATION_PROPERTIES = "application.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyModule.class);

    private final String[] args;

    public PropertyModule(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("args must be defined.");
        }
        this.args = args;
    }

    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    PropertyValueProvider propertyValueProvider() {
        LinkedList<PropertyValueProvider> valueProviders = new LinkedList<>();
        OrderedMergedValueProvider valueProvider = new OrderedMergedValueProvider(valueProviders);

        if (args.length > 0) {
            JavaArgumentPropertyValueProvider javaArgumentPropertyValueProvider = new JavaArgumentPropertyValueProvider(args);
            valueProviders.add(javaArgumentPropertyValueProvider);
        }

        SystemEnvValueProvider systemEnvValueProvider = new SystemEnvValueProvider();
        valueProviders.add(systemEnvValueProvider);

        SystemPropertyValueProvider systemPropertyValueProvider = new SystemPropertyValueProvider();
        valueProviders.add(systemPropertyValueProvider);

        RedisDockerLinkPropertyValueProvider redisDockerLinkPropertyValueProvider = new RedisDockerLinkPropertyValueProvider(systemEnvValueProvider);
        valueProviders.add(redisDockerLinkPropertyValueProvider);
/*
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(APPLICATION_CONFIGURATION_PROPERTIES)) {
            Properties properties = new Properties();
            properties.load(in);
            PropertiesValueProvider propertiesValueProvider = new PropertiesValueProvider(properties);
            valueProviders.add(propertiesValueProvider);
        } catch (IOException e) {
            LOGGER.error("Unable to load properties file " + APPLICATION_CONFIGURATION_PROPERTIES, e);
        }
        */
        return valueProvider;
    }

    @Provides
    @Singleton
    SecurityConfig provideSecurityConfig(PropertyValueProvider valueProvider) {
        return createConfig(SecurityConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    RedisConfig provideRedisConfig(PropertyValueProvider valueProvider) {
        return createConfig(RedisConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    KodokojoConfig provideKodokojoConfig(PropertyValueProvider valueProvider) {
        return createConfig(KodokojoConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    ApplicationConfig provideApplicationConfig(PropertyValueProvider valueProvider) {
        return createConfig(ApplicationConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    MarathonConfig provideMarathonConfig(PropertyValueProvider valueProvider) {
        return createConfig(MarathonConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    AwsConfig provideAwsConfig(PropertyValueProvider valueProvider) {
        return createConfig(AwsConfig.class, valueProvider);
    }

    private <T extends PropertyConfig> T createConfig(Class<T> configClass, PropertyValueProvider valueProvider) {
        PropertyResolver resolver = new PropertyResolver(valueProvider);
        return resolver.createProxy(configClass);
    }

}
