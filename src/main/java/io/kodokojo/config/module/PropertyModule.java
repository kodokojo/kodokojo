/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.config.KodokojoConfig;
import io.kodokojo.config.properties.PropertyConfig;
import io.kodokojo.config.properties.PropertyResolver;
import io.kodokojo.config.*;
import io.kodokojo.config.properties.provider.RedisDockerLinkPropertyValueProvider;
import io.kodokojo.config.properties.provider.*;
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

    @Provides
    @Singleton
    EmailConfig provideEmailConfig(PropertyValueProvider valueProvider) {
        return createConfig(EmailConfig.class, valueProvider);
    }


    @Provides
    @Singleton
    VersionConfig provideVersionConfig(PropertyValueProvider valueProvider) {
        return createConfig(VersionConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    ReCaptchaConfig provideReCaptchaConfig(PropertyValueProvider valueProvider) {
        return createConfig(ReCaptchaConfig.class, valueProvider);
    }

    private <T extends PropertyConfig> T createConfig(Class<T> configClass, PropertyValueProvider valueProvider) {
        PropertyResolver resolver = new PropertyResolver(valueProvider);
        return resolver.createProxy(configClass);
    }

}
