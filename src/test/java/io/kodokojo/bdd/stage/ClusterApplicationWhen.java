package io.kodokojo.bdd.stage;

import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.ProjectAlreadyExistException;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.ProjectStore;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class ClusterApplicationWhen<SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

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

        /*
        BrickFactory brickFactory = new DefaultBrickFactory(null);
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();

        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.JENKINS)));
        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.GITLAB)));
        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.HAPROXY), false));

        loadBalancerIp = "52.19.37.28";    //Ha proxy may be reloadable in a short future.
        StackConfiguration stackConfiguration = new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, loadBalancerIp, 10022);
        stackConfigurations.add(stackConfiguration);

        this.projectConfiguration = new ProjectConfiguration(projectName, currentUser.getEmail(), stackConfigurations, Collections.singletonList(currentUser));
        */

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setReadTimeout(10, TimeUnit.MINUTES);
        httpClient.setConnectTimeout(10, TimeUnit.MINUTES);
        httpClient.setWriteTimeout(10, TimeUnit.MINUTES);
        String url = "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/projectconfig";
        String auth = "Basic " + Base64.getEncoder().encodeToString((currentUser.getUsername() + ":" + currentUser.getPassword()).getBytes());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\n" +
                "  \"name\": \"" + projectName + "\",\n" +
                "  \"ownerIdentifier\": \"" + currentUser.getIdentifier() + "\"\n" +
                "}");
        Request request = new Request.Builder().post(body).url(url).addHeader("Authorization", auth).build();
        Response response = null;
        try {
            long begin = System.currentTimeMillis();
            response = httpClient.newCall(request).execute();
            long end = System.currentTimeMillis();
            System.out.println("Duration " + (end-begin)/1000);
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
                try {
                    response.body().close();
                } catch (IOException e) {
                    fail("Fail to close http body response", e);
                }
            }
        }

        return self();
    }

    public SELF i_start_the_project() {
        try {
            projectManager.start(projectConfiguration);
        } catch (ProjectAlreadyExistException e) {
            fail("Project already running", e);
        }
        return self();
    }


}
