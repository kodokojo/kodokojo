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
package io.kodokojo.commons.utils.properties.provider;

/*
 * #%L
 * commons-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerConfigValueProvider implements PropertyValueProvider {

    private final PropertyValueProvider delegate;

    public DockerConfigValueProvider(PropertyValueProvider delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must be defined.");
        }
        this.delegate = delegate;
    }

    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {

        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        T value = delegate.providePropertyValue(classType, key);

        if ("DOCKER_HOST".equals(key) && classType.isAssignableFrom(String.class) && value != null) {
            return (T) value.toString().replaceAll("tcp://", "https://");
        } else {
            return value;
        }
    }
}
