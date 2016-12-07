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
package io.kodokojo.commons.config.properties.provider;

import io.kodokojo.commons.config.RabbitMqConfig;

public class RabbitMqDockerLinkPropertyValueProvider implements PropertyValueProvider {

    private final PropertyValueProvider delegate;

    private final Integer rabbitmqPort;

    private final String rabbitmqHost;

    public RabbitMqDockerLinkPropertyValueProvider(PropertyValueProvider delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must be defined.");
        }
        this.delegate = delegate;
        rabbitmqPort = delegate.providePropertyValue(Integer.class, "RABBITMQ_PORT_5672_TCP_PORT");
        rabbitmqHost = delegate.providePropertyValue(String.class, "RABBITMQ_PORT_5672_TCP_ADDR");
    }

    public RabbitMqDockerLinkPropertyValueProvider() {
        this(new SystemEnvValueProvider());
    }

    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {
        if (rabbitmqPort != null) {
            if (RabbitMqConfig.RABBITMQ_HOST.equals(key) && String.class.isAssignableFrom(classType)) {
                return (T) rabbitmqHost;
            } else if (RabbitMqConfig.RABBITMQ_PORT.equals(key) && Integer.class.isAssignableFrom(classType)) {
                return (T) rabbitmqPort;
            }
        }
        return delegate.providePropertyValue(classType, key);
    }
}


