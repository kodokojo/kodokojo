package io.kodokojo.config.module;

import com.google.inject.Provider;
import io.kodokojo.config.RedisConfig;
import io.kodokojo.service.user.redis.RedisUserManager;

import javax.crypto.SecretKey;

public class RedisUserManagerProvider implements Provider<RedisUserManager> {

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
    public RedisUserManager get() {
        SecretKey secretKey = secretKeyProvider.get();
        RedisConfig redisConfig = redisConfigProvider.get();
        return new RedisUserManager(secretKey, redisConfig.host(), redisConfig.port());
    }
}
