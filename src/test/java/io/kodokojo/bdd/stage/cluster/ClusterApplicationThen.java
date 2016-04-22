package io.kodokojo.bdd.stage.cluster;

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
import io.kodokojo.service.user.redis.RedisUserManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
        try {
            Thread.sleep(90000);// Use future websocket endpoint to wait services started and configured
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    public SELF i_have_a_valid_repository() {
        checkHttpService("repository." + projectConfiguration.getName().toLowerCase() + ".kodokojo.io");
        return self();
    }

    private void checkHttpService(String serviceUrl) {
        OkHttpClient httpClient = provideDefaultOkHttpClient();
        Request request = new Request.Builder().url("https://" + serviceUrl).get().build();
        try {
            Response response = httpClient.newCall(request).execute();
            assertThat(response.code()).isBetween(200, 299);
        } catch (IOException e) {
            fail("Unable to request service URL " + serviceUrl,e);
        }
    }

    private OkHttpClient provideDefaultOkHttpClient() {
        OkHttpClient httpClient = new OkHttpClient();
        final TrustManager[] certs = new TrustManager[]{new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }
        }};

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (final java.security.GeneralSecurityException ex) {
        }
        httpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        httpClient.setSslSocketFactory(ctx.getSocketFactory());
        return httpClient;
    }
}
