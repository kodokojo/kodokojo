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

import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.properties.provider.SystemEnvValueProvider;
import io.kodokojo.config.RedisConfig;

public class RedisDockerLinkPropertyValueProvider implements PropertyValueProvider {

    private final PropertyValueProvider delegate;

    private final Integer redisPort;

    private final String redisHost;

    public RedisDockerLinkPropertyValueProvider(PropertyValueProvider delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must be defined.");
        }
        this.delegate = delegate;
        redisPort = delegate.providePropertyValue(Integer.class, "REDIS_PORT_6379_TCP_PORT");
        redisHost = delegate.providePropertyValue(String.class, "REDIS_PORT_6379_TCP_ADDR");
    }

    public RedisDockerLinkPropertyValueProvider() {
        this(new SystemEnvValueProvider());
    }

    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {
        if (redisPort != null) {
            if (RedisConfig.REDIS_HOST.equals(key) && String.class.isAssignableFrom(classType)) {
                return (T) redisHost;
            } else if (RedisConfig.REDIS_PORT.equals(key) && Integer.class.isAssignableFrom(classType)) {
                return (T) redisPort;
            }
        }
        return delegate.providePropertyValue(classType, key);
    }
}


