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
import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.bdd.stage.UserInfo;
import io.kodokojo.bdd.stage.WebSocketEventsListener;
import io.kodokojo.bdd.stage.brickauthenticator.GitlabUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.JenkinsUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.UserAuthenticator;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
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
        USER_AUTHENTICATOR.put("jenkins", new JenkinsUserAuthenticator());
    }

    @ProvidedScenarioState
    String marathonUrl;

    @ExpectedScenarioState
    User currentUser;

    @ExpectedScenarioState
    Session currentUserWebSocket;

    @ExpectedScenarioState
    WebSocketEventsListener webSocketEventsListener;

    @ExpectedScenarioState
    ProjectConfiguration projectConfiguration;

    @ExpectedScenarioState
    Map<String, UserInfo> currentUsers = new HashMap<>();


    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    public SELF i_have_a_valid_scm() {
        checkHttpService("scm", "gitlab");
        return self();
    }

    public SELF i_have_a_valid_ci() {
        checkHttpService("ci", "jenkins");
        return self();
    }

    public SELF i_have_a_valid_repository() {

        checkHttpService("repository", "nexus");
        return self();
    }

    private void checkHttpService(String brickType, String brickName) {


        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(WebSocketMessage.class, new WebSocketMessageGsonAdapter());
        Gson gson = builder.setPrettyPrinting().create();

        String projectDomaineName = projectConfiguration.getName().toLowerCase();
        String brickDomainUrl = "https://" + brickType + "-" + projectDomaineName + ".kodokojo.io";

        Callable<Boolean> inspectWebSocket = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean brickNameFound = false;
                long timeOutDate = System.currentTimeMillis() + 180000;
                boolean delta = true;
                int cursor = 0;
                do {
                    LinkedList<String> message = new LinkedList<>(webSocketEventsListener.getMessages());
                    int size = message.size();
                    LinkedList<String> newMessage = new LinkedList<>(message.subList(cursor, size));
                    int i = 0;
                    for (String msg : newMessage) {
                        LOGGER.debug("Receive new WebSocket message [{}|{}]: {}", cursor, i, msg);
                        i++;
                    }
                    cursor = size;
                    for (String m : message) {
                        //LOGGER.debug("message receives : {}", m);
                        WebSocketMessage webSocketMessage = gson.fromJson(m, WebSocketMessage.class);
                        if ("brick".equals(webSocketMessage.getEntity()) && "updateState".equals(webSocketMessage.getAction())) {
                            JsonObject data = webSocketMessage.getData();
                            if (data.has("projectConfiguration")) {
                                String projectConfigurationId = data.getAsJsonPrimitive("projectConfiguration").getAsString();
                                if (projectConfigurationId.equals(projectConfiguration.getIdentifier())
                                        && data.getAsJsonPrimitive("brickName").getAsString().equals(brickName)) {
                                    String state = data.getAsJsonPrimitive("state").getAsString();
                                    if (state.equals("ONFAILURE")) {
                                        throw new Exception("On Failure");
                                    }
                                    brickNameFound = state.equals("RUNNING");
                                }
                            }
                        }
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    delta = timeOutDate > System.currentTimeMillis();
                } while (!brickNameFound && delta);

                UserAuthenticator userAuthenticator = USER_AUTHENTICATOR.get(brickName);
                if (brickNameFound && userAuthenticator != null) {
                    boolean authenticate = userAuthenticator.authenticate(GitlabConfigurer.provideDefaultOkHttpClient(), brickDomainUrl, new UserInfo(currentUser.getUsername(), currentUser.getIdentifier(), currentUser.getPassword(), currentUser.getEmail()));
                    assertThat(authenticate).overridingErrorMessage("Unable to be authenticate on brick %s on URL %s", brickName, brickDomainUrl).isTrue();
                    brickNameFound = authenticate;
                }

                return brickNameFound;
            }
        };

        Future<Boolean> submit = Executors.newFixedThreadPool(1).submit(inspectWebSocket);
        try {
            Boolean foundRunning = submit.get(5, TimeUnit.MINUTES);
            LOGGER.debug("Brick {} is {} found as RUNNING", brickName, foundRunning ? "" : "NOT");
            assertThat(foundRunning).overridingErrorMessage("brick %s not found as RUNNING", brickName).isTrue();
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            fail(e.getMessage(), e);
        }

        OkHttpClient httpClient = provideDefaultOkHttpClient();
        Request request = new Request.Builder().url(brickDomainUrl).get().build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isBetween(200, 299);
        } catch (IOException e) {
            fail("Unable to request service URL " + brickDomainUrl, e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
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

    public SELF it_possible_to_log_on_brick_$_with_user_$(@Quoted String brickName, @Quoted String username) {
        OkHttpClient httpClient = provideDefaultOkHttpClient();
        UserAuthenticator userAuthenticator = USER_AUTHENTICATOR.get(brickName);
        assertThat(userAuthenticator).isNotNull();

        UserInfo userInfo = new UserInfo(this.currentUser);
        UserDto userDto = StageUtils.getUserDto(getApiBaseUrl(), userInfo, currentUsers.get(username).getIdentifier());
        ProjectDto projectDto = StageUtils.getProjectDto(getApiBaseUrl(), userInfo, userDto.getProjectConfigurationIds().get(0).getProjectId());

        MarathonServiceLocator serviceLocator = new MarathonServiceLocator(marathonUrl);
        Set<Service> services = serviceLocator.getService(new DefaultBrickFactory().createBrick(brickName).getType().name().toLowerCase(), projectDto.getName().toLowerCase());
        assertThat(services).isNotEmpty();
        Service service = services.iterator().next();
        String url = "http://" + service.getHost() + ":" + service.getPort();

        boolean authenticate = userAuthenticator.authenticate(httpClient, url, currentUsers.get(username));
        assertThat(authenticate).isTrue();
        return self();
    }


    private String getApiBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1";
    }
}
