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

import com.google.inject.Provider;
import io.kodokojo.config.RedisConfig;
import io.kodokojo.service.redis.RedisUserRepository;

import javax.crypto.SecretKey;

public class RedisUserManagerProvider implements Provider<RedisUserRepository> {

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
    public RedisUserRepository get() {
        SecretKey secretKey = secretKeyProvider.get();
        RedisConfig redisConfig = redisConfigProvider.get();
        return new RedisUserRepository(secretKey, redisConfig.host(), redisConfig.port());
    }
}
