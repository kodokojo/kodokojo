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
package io.kodokojo.service.marathon;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.kodokojo.config.MarathonConfig;
import io.kodokojo.model.PortDefinition;
import io.kodokojo.model.Service;
import io.kodokojo.service.ServiceLocator;
import io.kodokojo.utils.JsonUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang.StringUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

public class MarathonServiceLocator implements ServiceLocator {

    private final MarathonServiceLocatorRestApi marathonServiceLocatorRestApi;

    @Inject
    public MarathonServiceLocator(MarathonConfig marathonConfig) {
        if (marathonConfig == null) {
            throw new IllegalArgumentException("marathonConfig must be defined.");
        }
        marathonServiceLocatorRestApi = provideMarathonRestApi(marathonConfig);
    }

    protected MarathonServiceLocatorRestApi provideMarathonRestApi(MarathonConfig marathonConfig) {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(marathonConfig.url());
        if (StringUtils.isNotBlank(marathonConfig.login())) {
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(chain -> {
                String basicAuthenticationValue = "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", marathonConfig.login(), marathonConfig.password()).getBytes());
                Request authenticateRequest = chain.request().newBuilder()
                        .addHeader("Authorization", basicAuthenticationValue)
                        .build();
                return chain.proceed(authenticateRequest);
            });
            builder.client(httpClientBuilder.build());
        }
        Retrofit adapter = builder.build();
        return adapter.create(MarathonServiceLocatorRestApi.class);
    }

    @Override
    public Set<Service> getService(String type, String projectName) {
        Set<Service> res = new HashSet<>();
        Set<String> appIds = new HashSet<>();
        Call<JsonObject> allApplicationsCall = marathonServiceLocatorRestApi.getAllApplications();
        try {
            Response<JsonObject> applicationsResponse = allApplicationsCall.execute();
            JsonObject json = applicationsResponse.body();
            JsonArray apps = json.getAsJsonArray("apps");
            for (int i = 0; i < apps.size(); i++) {
                JsonObject app = (JsonObject) apps.get(i);
                String id = app.getAsJsonPrimitive("id").getAsString();
                JsonObject labels = app.getAsJsonObject("labels");
                if (labels.has("endpoint")) {
                    String project = labels.getAsJsonPrimitive("endpoint").getAsString();
                    if (labels.has("component")) {
                        String component = labels.getAsJsonPrimitive("component").getAsString();
                        if (projectName.equals(project) && type.equals(component)) {
                            appIds.add(id);
                        }
                    }
                }
            }
            for (String appId : appIds) {
                Call<JsonObject> confCall = marathonServiceLocatorRestApi.getApplicationConfiguration(appId);
                Response<JsonObject> confResponse = confCall.execute();
                JsonObject applicationConfiguration = confResponse.body();
                res.addAll(convertToService(projectName + "-" + type, applicationConfiguration));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    private static Set<Service> convertToService(String name, JsonObject json) {
        Set<Service> res = new HashSet<>();
        JsonObject app = json.getAsJsonObject("app");
        JsonObject container = app.getAsJsonObject("container");
        String containerType = container.getAsJsonPrimitive("type").getAsString();
        if ("DOCKER".equals(containerType)) {
            List<PortDefinition> ports = new ArrayList<>();
            JsonObject docker = container.getAsJsonObject("docker");
            JsonArray portMappings = docker.getAsJsonArray("portMappings");
            for (int i = 0; i < portMappings.size(); i++) {
                JsonObject portMapping = (JsonObject) portMappings.get(i);
                String containerPort = portMapping.getAsJsonPrimitive("containerPort").getAsString();

                PortDefinition.Type portType = JsonUtils.readJsonObjectFromJson(portMapping, "labels")
                        .map(lab -> JsonUtils.readStringFromJson(lab, "applicationProtocol"))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(PortDefinition.Type::valueOf)
                        .orElse(PortDefinition.Type.TCP);


                ports.add(new PortDefinition(portType, -1, Integer.parseInt(containerPort)));

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
                        PortDefinition portDefinition = ports.get(j);
                        PortDefinition portDefWithHostPort = new PortDefinition(portDefinition.getName(), portDefinition.getType(), jsonPort.getAsInt(), portDefinition.getContainerPort(), portDefinition.getServicePort());
                        res.add(new Service(name + "-" + portDefinition.getContainerPort(), host, portDefWithHostPort));
                    }
                }
            }
        }
        return res;
    }

}
