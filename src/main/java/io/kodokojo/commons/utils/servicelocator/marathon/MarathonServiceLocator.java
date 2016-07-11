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
package io.kodokojo.commons.utils.servicelocator.marathon;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import retrofit.RestAdapter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MarathonServiceLocator implements ServiceLocator {

    private final MarathonRestApi marathonRestApi;

    @Inject
    public MarathonServiceLocator(String marathonUrl) {
        if (isBlank(marathonUrl)) {
            throw new IllegalArgumentException("marathonUrl must be defined.");
        }
        marathonRestApi = provideMarathonRestApi(marathonUrl);
    }

    protected MarathonRestApi provideMarathonRestApi(String marathonUrl) {
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(marathonUrl).build();
        return adapter.create(MarathonRestApi.class);
    }

    @Override
    public Set<Service> getService(String type, String name) {
        Set<String> appIds = new HashSet<>();
        JsonObject json = marathonRestApi.getAllApplications();
        JsonArray apps = json.getAsJsonArray("apps");
        for (int i = 0; i < apps.size(); i++) {
            JsonObject app = (JsonObject) apps.get(i);
            String id = app.getAsJsonPrimitive("id").getAsString();
            JsonObject labels = app.getAsJsonObject("labels");
            if (labels.has("project")) {
                String project = labels.getAsJsonPrimitive("project").getAsString();
                String apptype = labels.getAsJsonPrimitive("componentType").getAsString();
                if (type.equals(apptype) && name.equals(project)) {
                    appIds.add(id);
                }
            }
        }
        Set<Service> res = new HashSet<>();
        for (String appId : appIds) {
            JsonObject applicationConfiguration = marathonRestApi.getApplicationConfiguration(appId);
            res.addAll(convertToService(name + "-" + type, applicationConfiguration));
        }

        return res;
    }

    @Override
    public Set<Service> getServiceByType(String type) {
        return null;
    }

    @Override
    public Set<Service> getServiceByName(String name) {
        return null;
    }

    private static Set<Service> convertToService(String name, JsonObject json) {
        Set<Service> res = new HashSet<>();
        JsonObject app = json.getAsJsonObject("app");
        JsonObject container = app.getAsJsonObject("container");
        String containerType = container.getAsJsonPrimitive("type").getAsString();
        if ("DOCKER".equals(containerType)) {
            List<String> ports = new ArrayList<>();
            JsonObject docker = container.getAsJsonObject("docker");
            JsonArray portMappings = docker.getAsJsonArray("portMappings");
            for (int i = 0; i < portMappings.size(); i++) {
                JsonObject portMapping = (JsonObject) portMappings.get(i);
                ports.add(portMapping.getAsJsonPrimitive("containerPort").getAsString());
            }
            JsonArray tasks = app.getAsJsonArray("tasks");
            for (int i = 0; i < tasks.size(); i++) {
                JsonObject task = (JsonObject) tasks.get(i);
                String host = task.getAsJsonPrimitive("host").getAsString();
                boolean alive = false;
                if (task.has("healthCheckResults")) {
                    JsonArray healthCheckResults = task.getAsJsonArray("healthCheckResults");
                    for (int j = 0; j < healthCheckResults.size() && !alive; j++) {
                        JsonObject healthCheck = (JsonObject) healthCheckResults.get(j);
                        alive = healthCheck.getAsJsonPrimitive("alive").getAsBoolean();
                    }
                }
                if (alive) {
                    JsonArray jsonPorts = task.getAsJsonArray("ports");
                    for (int j = 0; j < jsonPorts.size(); j++) {
                        JsonPrimitive jsonPort = (JsonPrimitive) jsonPorts.get(j);
                        String portName = ports.get(j);
                        res.add(new Service(name + "-" + portName, host, jsonPort.getAsInt()));
                    }
                }
            }
        }
        return res;
    }

}
