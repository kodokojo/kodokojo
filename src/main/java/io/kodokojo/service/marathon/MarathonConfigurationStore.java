package io.kodokojo.service.marathon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.lifecycle.ApplicationLifeCycleListener;
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
    public boolean storeSSLKeys(String project, String entityType, SSLKeyPair sslKeyPair) {
        if (isBlank(project)) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (isBlank(entityType)) {
            throw new IllegalArgumentException("entityType must be defined.");
        }
        if (sslKeyPair == null) {
            throw new IllegalArgumentException("sslKeyPair must be defined.");
        }
        Response response = null;
        try {
            Writer writer = new StringWriter();
            SSLUtils.writeSSLKeyPairPem(sslKeyPair, writer);
            byte[] certificat = writer.toString().getBytes();

            String url = marathonUrl + "/v2/artifacts/ssl/" + project.toLowerCase() + "/" + entityType.toLowerCase() + "/" + project.toLowerCase() + "-" + entityType.toLowerCase() + "-server.pem";
            OkHttpClient httpClient = new OkHttpClient();
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", project + "-" + entityType + "-server.pem",
                            RequestBody.create(MediaType.parse("application/text"), certificat))
                    .build();
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = httpClient.newCall(request).execute();
            return response.code() == 200;
        } catch (IOException e) {
            LOGGER.error("Unable to store ssl key for project {} and brick {}", project, entityType, e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }

}
