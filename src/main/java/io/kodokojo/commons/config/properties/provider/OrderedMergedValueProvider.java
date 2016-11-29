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
package io.kodokojo.commons.config.properties.provider;

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


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class OrderedMergedValueProvider implements PropertyValueProvider {

    private final List<PropertyValueProvider> propertyValueProviders;

    public OrderedMergedValueProvider(LinkedList<PropertyValueProvider> propertyValueProviders) {
        if (propertyValueProviders == null) {
            throw new IllegalArgumentException("propertyValueProviders must be defined.");
        }
        this.propertyValueProviders = propertyValueProviders;
    }


    @Override
    public <T> T providePropertyValue(Class<T> classType, String key) {
        if (classType == null) {
            throw new IllegalArgumentException("classType must be defined.");
        }
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        T res = null;
        Iterator<? extends PropertyValueProvider> iterator = propertyValueProviders.iterator();
        while (res == null && iterator.hasNext()) {
            PropertyValueProvider valueProvider = iterator.next();
            res = valueProvider.providePropertyValue(classType, key);
        }
        return res;
    }
}
