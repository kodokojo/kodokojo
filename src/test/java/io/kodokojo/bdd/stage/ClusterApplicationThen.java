package io.kodokojo.bdd.stage;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.commons.model.Service;
import io.kodokojo.entrypoint.RestEntrypoint;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.user.redis.RedisUserManager;

import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClusterApplicationThen<SELF extends ClusterApplicationThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    RestEntrypoint restEntrypoint;

    @ExpectedScenarioState
    MarathonIsPresent marathon;

    @ExpectedScenarioState
    Service redisService;

    @ExpectedScenarioState
    List<Service> services;

    @ExpectedScenarioState
    RedisUserManager redisUserManager;

    @ExpectedScenarioState
    String testId;

    @ExpectedScenarioState
    User currentUser;

    @ExpectedScenarioState
    KeyPair userKeyPair;

    @ExpectedScenarioState
    ProjectConfiguration projectConfiguration;

    public SELF i_have_a_valid_scm() {
        checkHttpService("scm." + projectConfiguration.getName().toLowerCase() + ".kodokojo.io");
        return self();
    }

    public SELF i_have_a_valid_ci() {
        checkHttpService("ci." + projectConfiguration.getName().toLowerCase() + ".kodokojo.io");
        return self();
    }

    public SELF i_have_a_valid_repo() {
        checkHttpService("repo." + projectConfiguration.getName().toLowerCase() + ".kodokojo.io");
        return self();
    }

    private void checkHttpService(String serviceUrl) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(serviceUrl).get().build();
        try {
            Response response = httpClient.newCall(request).execute();
            assertThat(response.code()).isBetween(200, 299);
        } catch (IOException e) {
            fail("Unable to request service URL " + serviceUrl);
        }
    }

}
