package io.kodokojo.commons.utils.servicelocator.consul;

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

import com.google.gson.*;
import io.kodokojo.commons.config.KodokojoConfig;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;

import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ConsulServiceLocator implements ServiceLocator {

    public static final String ADDRESS_KEY = "Address";

    public static final String SERVICE_PORT_KEY = "ServicePort";

    private final String kodokojoTags;

    private final ConsulRest consulRest;

    public ConsulServiceLocator(ConsulRest consulRest, KodokojoConfig kodokojoConfig) {
        if (consulRest == null) {
            throw new IllegalArgumentException("consulRest must be defined.");
        }
        this.consulRest = consulRest;
        this.kodokojoTags = new StringBuilder()
                .append(KODOKOJO_PREFIXE).append("=").append(kodokojoConfig.projectName()).append(",")
                .append(STACK_NAME_KEY).append("=").append(kodokojoConfig.stackName()).append(",")
                .append(STACK_TYPE_KEY).append("=").append(kodokojoConfig.stackType())
                .toString();
    }


    @Override
    public Set<Service> getService(String type, String name) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        String serviceName = name;
        StringBuilder tags = new StringBuilder().append(kodokojoTags).append(",")
                .append(COMPONENT_NAME_KEY).append("=").append(name).append(",")
                .append(COMPONENT_TYPE_KEY).append("=").append(type);
        JsonArray resultsJson = consulRest.getServices(serviceName, kodokojoTags);

        if (resultsJson == null || resultsJson.size() == 0) {
            return null;    //Search not return any service. return null.
        }

        // Iterate to extra multiples tag->entrypoint.
        Set<Service> entryPoints = new HashSet<>();
        for (JsonElement jsonElement : resultsJson) {
            if (!jsonElement.isJsonObject())
                throw new IllegalStateException("Unexpected response return by consul. Waiting a json object, get " + jsonElement.toString());

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String host = jsonObject.get(ADDRESS_KEY).getAsString();
            int servicePort = jsonObject.get(SERVICE_PORT_KEY).getAsInt();

            Service entryPoint = new Service(host, host, servicePort);
            entryPoints.add(entryPoint);
        }

        return entryPoints;
    }

    @Override
    public Set<Service> getServiceByType(String type) {
        return null;
    }

    @Override
    public Set<Service> getServiceByName(String name) {
        return null;
    }

}
