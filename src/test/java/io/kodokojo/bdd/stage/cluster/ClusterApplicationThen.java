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
package io.kodokojo.bdd.stage.cluster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.bdd.stage.HttpUserSupport;
import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.bdd.stage.UserInfo;
import io.kodokojo.bdd.stage.WebSocketEventsListener;
import io.kodokojo.bdd.stage.brickauthenticator.GitlabUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.JenkinsUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.NexusUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.UserAuthenticator;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.brick.nexus.NexusConfigurer;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.entrypoint.dto.ProjectDto;
import io.kodokojo.entrypoint.dto.UserDto;
import io.kodokojo.entrypoint.dto.WebSocketMessage;
import io.kodokojo.entrypoint.dto.WebSocketMessageGsonAdapter;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.websocket.Session;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClusterApplicationThen<SELF extends ClusterApplicationThen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationThen.class);

    private final static Map<String, UserAuthenticator> USER_AUTHENTICATOR;

    static {
        USER_AUTHENTICATOR = new HashMap<>();
        USER_AUTHENTICATOR.put("gitlab", new GitlabUserAuthenticator());
        USER_AUTHENTICATOR.put("nexus", new NexusUserAuthenticator());
        USER_AUTHENTICATOR.put("jenkins", new JenkinsUserAuthenticator());
    }

    @ProvidedScenarioState
    String marathonUrl;

    @ExpectedScenarioState
    UserInfo currentUser;

    @ExpectedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();

    @ExpectedScenarioState
    HttpUserSupport httpUserSupport;

    public SELF it_possible_to_log_on_brick_$_with_user_$(@Quoted String brickName, @Quoted String username) {
        OkHttpClient httpClient = provideDefaultOkHttpClient();
        UserAuthenticator userAuthenticator = USER_AUTHENTICATOR.get(brickName);
        assertThat(userAuthenticator).isNotNull();


        UserDto userDto = httpUserSupport.getUserDto(currentUser, currentUsers.get(username).getIdentifier());
        ProjectDto projectDto = httpUserSupport.getProjectDto(currentUser, userDto.getProjectConfigurationIds().get(0).getProjectId());

        MarathonServiceLocator serviceLocator = new MarathonServiceLocator(marathonUrl);
        Set<Service> services = serviceLocator.getService(new DefaultBrickFactory().createBrick(brickName).getType().name().toLowerCase(), projectDto.getName().toLowerCase());
        assertThat(services).isNotEmpty();
        Service service = services.iterator().next();
        String url = "http://" + service.getHost() + ":" + service.getPort();

        boolean authenticate = userAuthenticator.authenticate(httpClient, url, currentUsers.get(username));
        assertThat(authenticate).isTrue();
        return self();
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
