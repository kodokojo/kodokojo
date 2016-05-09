package io.kodokojo.config.module;

import com.google.inject.Provider;
import io.kodokojo.config.RedisConfig;
import io.kodokojo.service.redis.RedisUserStore;

import javax.crypto.SecretKey;

public class RedisUserManagerProvider implements Provider<RedisUserStore> {

    private final Provider<SecretKey> secretKeyProvider;

    private final Provider<RedisConfig> redisConfigProvider;

    public RedisUserManagerProvider(Provider<SecretKey> secretKeyProvider, Provider<RedisConfig> redisConfigProvider) {
        if (secretKeyProvider == null) {
            throw new IllegalArgumentException("secretKeyProvider must be defined.");
        }
        if (redisConfigProvider == null) {
            throw new IllegalArgumentException("redisConfigProvider must be defined.");
        }
        this.secretKeyProvider = secretKeyProvider;
        this.redisConfigProvider = redisConfigProvider;
    }

    @Override
    public RedisUserStore get() {
        SecretKey secretKey = secretKeyProvider.get();
        RedisConfig redisConfig = redisConfigProvider.get();
        return new RedisUserStore(secretKey, redisConfig.host(), redisConfig.port());
    }
}
