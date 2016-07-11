package io.kodokojo.commons.utils.docker;

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


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.squareup.okhttp.*;
import io.kodokojo.commons.config.DockerConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerSupport.class);

    private final DockerConfig dockerConfig;

    @Inject
    public DockerSupport(DockerConfig dockerConfig) {
        if (dockerConfig == null) {
            throw new IllegalArgumentException("dockerConfig must be defined.");
        }
        this.dockerConfig = dockerConfig;
    }

    public DockerClient createDockerClient() {
        DockerClientConfig config;
        if (StringUtils.isBlank(dockerConfig.dockerServerUrl())) {
            config = DockerClientConfig.createDefaultConfigBuilder().build();
        } else {
            DockerClientConfig.DockerClientConfigBuilder builder = DockerClientConfig.createDefaultConfigBuilder();
            File dockerCerts = new File(dockerConfig.dockerCertPath());
            if (dockerCerts.exists() && dockerCerts.canRead()) {
                builder.withDockerCertPath(dockerConfig.dockerCertPath());
            }
            config = builder
                    .withUri(dockerConfig.dockerServerUrl())
                    .build();
        }
        return DockerClientBuilder.getInstance(config).build();
    }

    public String getDockerHost() {
        String dockerServerUrl = dockerConfig.dockerServerUrl();
        if (StringUtils.isBlank(dockerServerUrl) || "unix:///var/run/docker.sock".equals(dockerServerUrl)) {
            return "localhost";
        }

        return dockerServerUrl.replaceAll("^http(s)?://", "").replaceAll(":\\d+$", "");
    }

    public int getExposedPort(String containerId, int containerPort) {
        InspectContainerResponse inspectContainerResponse = createDockerClient().inspectContainerCmd(containerId).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();
        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(containerPort));
        if (bindingsExposed == null) {
            return -1;
        }
        return bindingsExposed[0].getHostPort();
    } public interface ServiceIsUp {
        boolean accept(Response response);
    }

    public boolean waitUntilHttpRequestRespond(String url, long time, ServiceIsUp serviceIsUp) {
        return waitUntilHttpRequestRespond(url, time, null, serviceIsUp);

    }
    public boolean waitUntilHttpRequestRespond(String url, long time) {
        return waitUntilHttpRequestRespond(url, time, null, Response::isSuccessful);
    }

    public boolean waitUntilHttpRequestRespond(String url, long time, TimeUnit unit, ServiceIsUp serviceIsUp) {
        if (isBlank(url)) {
            throw new IllegalArgumentException("url must be defined.");
        }

        long now = System.currentTimeMillis();
        long delta = unit != null ? TimeUnit.MILLISECONDS.convert(time, unit) : time;
        long endTime = now + delta;
        long until = 0;


        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl httpUrl = HttpUrl.parse(url);

        int nbTry = 0;
        boolean available = false;
        do {
            nbTry++;
            available = tryRequest(httpUrl, httpClient, serviceIsUp);
            if (!available) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    break;
                }
                now = System.currentTimeMillis();
                until = endTime - now;
            }
        } while (until > 0 && !available);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(url + " " + (available ? "Success" : "Failed after " + nbTry + " try"));
        }
        return available;
    }

    private boolean tryRequest(HttpUrl url, OkHttpClient httpClient, ServiceIsUp serviceIsUp) {
        Response response = null;
        try {
            Request request = new Request.Builder().url(url).get().build();
            Call call = httpClient.newCall(request);
            response = call.execute();
            boolean isSuccesseful = serviceIsUp.accept(response);
            response.body().close();
            return isSuccesseful;
        } catch (IOException e) {
            return false;
        } finally {
            if (response != null) {
                try {
                    response.body().close();
                } catch (IOException e) {
                    LOGGER.warn("Unable to close response.");
                }
            }
        }
    }
}
