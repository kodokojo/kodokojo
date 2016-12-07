/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.*;
import io.kodokojo.commons.config.properties.PropertyConfig;
import io.kodokojo.commons.config.properties.PropertyResolver;
import io.kodokojo.commons.config.properties.provider.*;
import io.kodokojo.commons.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

public class CommonsPropertyModule extends AbstractModule {

    public static final String APPLICATION_CONFIGURATION_PROPERTIES = "application.properties";

    public static final String VERSION_CONFIGURATION_PROPERTIES = "version.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonsPropertyModule.class);

    private final String[] args;

    public CommonsPropertyModule(String[] args) {
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

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(VERSION_CONFIGURATION_PROPERTIES)) {
            Properties properties = new Properties();
            properties.load(in);
            PropertiesValueProvider propertiesValueProvider = new PropertiesValueProvider(properties);
            valueProviders.add(propertiesValueProvider);
        } catch (IOException e) {
            LOGGER.error("Unable to load properties file " + VERSION_CONFIGURATION_PROPERTIES, e);
        }

        return valueProvider;
    }

    @Provides
    @Singleton
    SecurityConfig provideSecurityConfig(PropertyValueProvider valueProvider) {
        return createConfig(SecurityConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    ApplicationConfig provideApplicationConfig(PropertyValueProvider valueProvider) {
        return createConfig(ApplicationConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    VersionConfig provideVersionConfig(PropertyValueProvider valueProvider) {
        return createConfig(VersionConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    MicroServiceConfig provideMicroServiceConfig(PropertyValueProvider valueProvider) {
        return createConfig(MicroServiceConfig.class, new MicroServiceValueProvider(valueProvider));
    }

    @Provides
    @Singleton
    RabbitMqConfig provideRabbitMqConfig(MicroServiceConfig microServiceConfig, PropertyValueProvider valueProvider) {
        return createConfig(RabbitMqConfig.class, new RabbitMqValueProvider(microServiceConfig, valueProvider));
    }


    @Provides
    @Singleton
    RedisConfig provideRedisConfig(PropertyValueProvider valueProvider) {
        return createConfig(RedisConfig.class, new RedisDockerLinkPropertyValueProvider(valueProvider));
    }

    @Provides
    @Singleton
    ServiceInfo provideServiceInfo(MicroServiceConfig microServiceConfig, VersionConfig versionConfig) {
        return new ServiceInfo(microServiceConfig.name(), microServiceConfig.uuid(), versionConfig.version(), versionConfig.gitSha1(), versionConfig.branch());
    }

    private <T extends PropertyConfig> T createConfig(Class<T> configClass, PropertyValueProvider valueProvider) {
        PropertyResolver resolver = new PropertyResolver(valueProvider);
        return resolver.createProxy(configClass);
    }

}
