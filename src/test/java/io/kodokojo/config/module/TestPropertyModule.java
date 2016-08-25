package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.config.*;
import io.kodokojo.config.properties.PropertyConfig;
import io.kodokojo.config.properties.PropertyResolver;
import io.kodokojo.config.properties.provider.*;

import java.util.LinkedList;

public class TestPropertyModule extends AbstractModule {

    private final String[] args;

    private final DockerTestSupport dockerTestSupport;
    private final int httpPort;
    private final String redisHost;
    private final int redisPort;

    public TestPropertyModule(String[] args, DockerTestSupport dockerTestSupport, int httpPort, String redisHost, int redisPort) {
        this.httpPort = httpPort;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        if (args == null) {
            this.args = new String[0];
        } else {
            this.args = args;

        }
        if (dockerTestSupport == null) {
            throw new IllegalArgumentException("dockerTestSupport must be defined.");
        }
        this.dockerTestSupport = dockerTestSupport;
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
        return valueProvider;
    }

    @Provides
    @Singleton
    SecurityConfig provideSecurityConfig(PropertyValueProvider valueProvider) {
        return createConfig(SecurityConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    RedisConfig provideRedisConfig() {
        return new RedisConfig() {
            @Override
            public String host() {
                return redisHost;
            }

            @Override
            public Integer port() {
                return redisPort;
            }
        };
    }

    @Provides
    @Singleton
    KodokojoConfig provideKodokojoConfig(PropertyValueProvider valueProvider) {
        return createConfig(KodokojoConfig.class, valueProvider);
    }

    @Provides
    @Singleton
    ApplicationConfig provideApplicationConfig() {
        return new ApplicationConfig() {
            @Override
            public int port() {
                return httpPort;
            }

            @Override
            public String domain() {
                return "kodokojo.dev";
            }

            @Override
            public String loadbalancerHost() {
                return dockerTestSupport.getServerIp();
            }

            @Override
            public int initialSshPort() {
                return 1022;
            }

            @Override
            public long sslCaDuration() {
                return -1;
            }
        };
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
    EmailConfig provideEmailConfig() {
        return new EmailConfig() {
            @Override
            public String smtpHost() {
                return null;
            }

            @Override
            public int smtpPort() {
                return 0;
            }

            @Override
            public String smtpUsername() {
                return null;
            }

            @Override
            public String smtpPassword() {
                return null;
            }

            @Override
            public String smtpFrom() {
                return "test@kodokojo.dev";
            }

        };
    }

    private <T extends PropertyConfig> T createConfig(Class<T> configClass, PropertyValueProvider valueProvider) {
        PropertyResolver resolver = new PropertyResolver(valueProvider);
        return resolver.createProxy(configClass);
    }

}

