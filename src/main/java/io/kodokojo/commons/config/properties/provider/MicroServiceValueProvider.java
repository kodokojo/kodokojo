/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
