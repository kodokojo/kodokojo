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
package io.kodokojo.service.marathon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.BootstrapStackData;
import io.kodokojo.service.ConfigurationStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MarathonConfigurationStore implements ConfigurationStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonConfigurationStore.class);

    private final String marathonUrl;

    @Inject
    public MarathonConfigurationStore(String marathonUrl) {
        if (isBlank(marathonUrl)) {
            throw new IllegalArgumentException("marathonUrl must be defined.");
        }
        this.marathonUrl = marathonUrl;
    }

    @Override
    public boolean storeBootstrapStackData(BootstrapStackData bootstrapStackData) {
        if (bootstrapStackData == null) {
            throw new IllegalArgumentException("bootstrapStackData must be defined.");
        }
        String url = marathonUrl + "/v2/artifacts/config/" + bootstrapStackData.getProjectName().toLowerCase() + ".json";
        OkHttpClient httpClient = new OkHttpClient();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(bootstrapStackData);
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", bootstrapStackData.getProjectName().toLowerCase() + ".json",
                        RequestBody.create(MediaType.parse("application/json"), json.getBytes()))
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.code() == 200;
        } catch (IOException e) {
            LOGGER.error("Unable to store configuration for project {}", bootstrapStackData.getProjectName(), e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }

    @Override
    public boolean storeSSLKeys(String project, String entityName, SSLKeyPair sslKeyPair) {
        if (isBlank(project)) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (isBlank(entityName)) {
            throw new IllegalArgumentException("entityName must be defined.");
        }
        if (sslKeyPair == null) {
            throw new IllegalArgumentException("sslKeyPair must be defined.");
        }
        Response response = null;
        try {
            Writer writer = new StringWriter();
            SSLUtils.writeSSLKeyPairPem(sslKeyPair, writer);
            byte[] certificat = writer.toString().getBytes();

            String url = marathonUrl + "/v2/artifacts/ssl/" + project.toLowerCase() + "/" + entityName.toLowerCase() + "/" + project.toLowerCase() + "-" + entityName.toLowerCase() + "-server.pem";
            OkHttpClient httpClient = new OkHttpClient();
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", project + "-" + entityName + "-server.pem",
                            RequestBody.create(MediaType.parse("application/text"), certificat))
                    .build();
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = httpClient.newCall(request).execute();
            int code = response.code();
            if (code >= 200 && code < 300) {
                LOGGER.info("Push SSL certificate on marathon url '{}' [content-size={}]", url, certificat.length);
            } else {
                LOGGER.error("Fail to push SSL certificate on marathon url '{}' status code {}. Body response:\n{}", url, code, response.body().string());
            }
            return code > 200 && code < 300;
        } catch (IOException e) {
            LOGGER.error("Unable to store ssl key for project {} and brick {}", project, entityName, e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }

}
