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
package io.kodokojo.commons.utils.properties;

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

import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PropertyResolver {

    private final PropertyValueProvider propertyValueProvider;

    private final InternalInvoker internalInvoker;

    public PropertyResolver(PropertyValueProvider propertyValueProvider) {
        if (propertyValueProvider == null) {
            throw new IllegalArgumentException("propertyValueProvider must be defined.");
        }
        this.propertyValueProvider = propertyValueProvider;
        this.internalInvoker = new InternalInvoker();
    }

    public <T extends PropertyConfig> T createProxy(Class<T> propertyConfig) {
        return (T) Proxy.newProxyInstance(PropertyResolver.class.getClassLoader(), new Class[]{propertyConfig}, internalInvoker);
    }

    private class InternalInvoker implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Key keyAnnotation = method.getAnnotation(Key.class);
            if (keyAnnotation == null) {
                return method.invoke(proxy, args);
            }
            Object value = propertyValueProvider.providePropertyValue(method.getReturnType(), keyAnnotation.value());
            if (isNotBlank(keyAnnotation.defaultValue()) && value == null) {
                value = getDefaultValue(method.getReturnType(), keyAnnotation.defaultValue());
            }
            return value;
        }
    }

    private static Object getDefaultValue(Class<?> expectedType, Object value){
        if (String.class.isAssignableFrom(expectedType)) {
            return value.toString();
        } else if (Integer.class.isAssignableFrom(expectedType) || int.class.isAssignableFrom(expectedType)) {
            return Integer.parseInt(value.toString());
        } else if (Long.class.isAssignableFrom(expectedType) || long.class.isAssignableFrom(expectedType)) {
            return Long.parseLong(value.toString());
        }  else if (BigDecimal.class.isAssignableFrom(expectedType)) {
            return new BigDecimal(value.toString());
        } else if (Double.class.isAssignableFrom(expectedType) || double.class.isAssignableFrom(expectedType)) {
            return Double.valueOf(value.toString());
        } else if (Boolean.class.isAssignableFrom(expectedType) || boolean.class.isAssignableFrom(expectedType)) {
            return Boolean.parseBoolean(value.toString());
        }
        return value;
    }
}

