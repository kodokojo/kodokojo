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
package io.kodokojo.service.servicelocator.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Filters;
import io.kodokojo.config.KodokojoConfig;
import io.kodokojo.model.Service;
import io.kodokojo.service.servicelocator.ServiceLocator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerServiceLocator implements ServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerServiceLocator.class);

    private final DockerSupport dockerSupport;

    private final DockerClient dockerClient;

    private final KodokojoConfig kodokojoConfig;

    @Inject
    public DockerServiceLocator(DockerSupport dockerSupport, KodokojoConfig kodokojoConfig) {
        if (dockerSupport == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        if (kodokojoConfig == null) {
            throw new IllegalArgumentException("kodokojoConfig must be defined.");
        }
        this.kodokojoConfig = kodokojoConfig;
        this.dockerSupport = dockerSupport;
        this.dockerClient = dockerSupport.createDockerClient();
    }

    @Override
    public Set<Service> getService(String type, String name) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        List<String> labels = new ArrayList<>(Arrays.asList(
                COMPONENT_TYPE_KEY+ "=" + type,
                COMPONENT_NAME_KEY + "=" + name)
        );

        Set<Service> services = searchServicesWithLabel(labels);

        if (services == null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("No container match name {}.", name);
        }
        return services;
    }

    @Override
    public Set<Service> getServiceByType(String type) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        List<String> labels = new ArrayList<>(Collections.singletonList(COMPONENT_TYPE_KEY + "=" + type));

        Set<Service> services = searchServicesWithLabel(labels);

        if (services == null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("No container match type {}.", type);
        }
        return services;
    }

    @Override
    public Set<Service> getServiceByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }

        List<String> labels = new ArrayList<>(Collections.singletonList(COMPONENT_NAME_KEY + "=" + name));

        Set<Service> services = searchServicesWithLabel(labels);

        if (services == null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("No container match name {}.", name);
        }
        return services;
    }

    private Set<Service> searchServicesWithLabel(List<String> labels) {
        assert labels != null : "labels must be defined";
        labels.add(PROJECT_KEY + "=" + kodokojoConfig.projectName());
        labels.add(STACK_NAME_KEY+ "=" + kodokojoConfig.stackName());
        labels.add(STACK_TYPE_KEY + "=" + kodokojoConfig.stackType());
        Filters filters = new Filters()
                .withLabels(labels.toArray(new String[]{}));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DockerServiceLocator lookup container with following criteria {}", filters.toString());
        }
        List<Container> containers = dockerClient.listContainersCmd().withFilters(filters).exec();

        if (CollectionUtils.isNotEmpty(containers)) {
            Set<Service> res = new HashSet<>(containers.size());
            for (Container container : containers) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Lookup list of public port for container : {}", container.getId());
                }
                for (Container.Port port : container.getPorts()) {
                    if (port.getPublicPort() != null && port.getPublicPort() > 0) {
                        String name = container.getLabels().get(KODOKOJO_PREFIXE + "componentName");
                        res.add(new Service(name, dockerSupport.getDockerHost(), port.getPublicPort()));
                    }
                }
            }
            return res;
        }
        return null;
    }
}
