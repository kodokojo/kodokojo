package io.kodokojo.commons.utils.servicelocator;

/*
 * #%L
 * docker-commons
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

import io.kodokojo.commons.model.Service;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MergedServiceLocator implements ServiceLocator {

    private final LinkedList<ServiceLocator> serviceLocators;

    public MergedServiceLocator(LinkedList<ServiceLocator> serviceLocators) {
        if (serviceLocators == null) {
            throw new IllegalArgumentException("serviceLocators must be defined.");
        }
        this.serviceLocators = serviceLocators;
    }

    @Override
    public Set<Service> getService(String type, String name) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return callAllServiceLocators((serviceLocator) -> serviceLocator.getService(type, name));
    }

    @Override
    public Set<Service> getServiceByType(String type) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        return callAllServiceLocators((serviceLocator) -> serviceLocator.getServiceByType(type));
    }

    @Override
    public Set<Service> getServiceByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return callAllServiceLocators((serviceLocator) -> serviceLocator.getServiceByName(name));
    }

    private Set<Service> callAllServiceLocators(Callback callback) {
        Set<Service> res = new HashSet<>();
        for (ServiceLocator serviceLocator : serviceLocators) {
            Set<Service> service = callback.execute(serviceLocator);
            if (CollectionUtils.isNotEmpty(service)) {
                res.addAll(service);
            }
        }
        return res.size() == 0 ? null : res;
    }

    interface Callback {
        Set<Service> execute(ServiceLocator serviceLocator);
    }
}
