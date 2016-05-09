package io.kodokojo.bdd.stage.cluster;

import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.store.ProjectStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class ClusterApplicationWhen<SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationWhen.class);

    @ExpectedScenarioState
    User currentUser;

    @ExpectedScenarioState
    ProjectManager projectManager;

    @ExpectedScenarioState
    ProjectStore projectStore;


    @ProvidedScenarioState
    ProjectConfiguration projectConfiguration;

    @ProvidedScenarioState
    String loadBalancerIp;

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    public SELF i_start_a_default_project_with_name_$(String projectName) {
        OkHttpClient httpClient = new OkHttpClient();
        /*
        httpClient.setReadTimeout(10, TimeUnit.MINUTES);
        httpClient.setConnectTimeout(10, TimeUnit.MINUTES);
        httpClient.setWriteTimeout(10, TimeUnit.MINUTES);
        */
        String url = "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/projectconfig";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\n" +
                "  \"name\": \"" + projectName + "\",\n" +
                "  \"ownerIdentifier\": \"" + currentUser.getIdentifier() + "\"\n" +
                "}");
        Request.Builder builder = new Request.Builder().post(body).url(url);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
        Response response = null;
        try {
            long begin = System.currentTimeMillis();
            response = httpClient.newCall(request).execute();
            long end = System.currentTimeMillis();
            LOGGER.trace("Project creation duration " + (end-begin)/1000);
            String identifier = response.body().string();
            assertThat(identifier).isNotEmpty();
            ProjectConfiguration configuration = projectStore.getProjectConfigurationById(identifier);
            assertThat(response.code()).isEqualTo(201);
            assertThat(configuration).isNotNull();
//            assertThat(configuration.getStackConfigurations()).isNotEmpty();
            projectConfiguration = configuration;
            loadBalancerIp = projectConfiguration.getDefaultStackConfiguration().getLoadBalancerIp();
        } catch (IOException e) {
            fail("Unable to request RestEntryPoint", e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        return self();
    }

    public SELF i_start_the_project() {

        OkHttpClient httpClient = new OkHttpClient();

        String url = "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/project/" + projectConfiguration.getIdentifier();
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Response response = null;

        try {
            response = httpClient.newCall(builder.build()).execute();
            assertThat(response.code()).isEqualTo(201);
            LOGGER.trace("Starting project");
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        return self();
    }


}
