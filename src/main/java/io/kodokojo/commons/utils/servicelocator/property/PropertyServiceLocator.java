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
package io.kodokojo.commons.utils.servicelocator.property;


import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class PropertyServiceLocator implements ServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyServiceLocator.class);

    private static final String HOST_KEY = "host";

    private static final String PORT_KEY = "port";

    private final PropertyValueProvider propertyValueProvider;

    public PropertyServiceLocator(PropertyValueProvider propertyValueProvider) {
        if (propertyValueProvider == null) {
            throw new IllegalArgumentException("propertyValueProvider must be defined.");
        }
        this.propertyValueProvider = propertyValueProvider;
    }

    @Override
    public Set<Service> getService(String type, String name) {
        return Collections.emptySet();
    }

    @Override
    public Set<Service> getServiceByType(String type) {
        return Collections.emptySet();
    }

    @Override
    public Set<Service> getServiceByName(String name) {
        String hostKey = name + "." + HOST_KEY;
        String portKey = name + "." + PORT_KEY;

        String host = propertyValueProvider.providePropertyValue(String.class, hostKey);
        Integer port = propertyValueProvider.providePropertyValue(Integer.class, portKey);

        if (StringUtils.isNotBlank(host) && port > 0) {
            Service service = new Service(name, host, port);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("BrickEntity name '{} found under PropertyProvider : {}", name, service);
            }
            return Collections.singleton(service);
        }

        return Collections.emptySet();
    }
}
