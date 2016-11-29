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
package io.kodokojo.bdd.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.commons.endpoint.dto.BrickConfigDto;
import io.kodokojo.commons.endpoint.dto.ProjectDto;
import io.kodokojo.commons.endpoint.dto.StackConfigDto;
import io.kodokojo.commons.endpoint.dto.UserDto;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class HttpUserSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUserSupport.class);

    private final OkHttpClient httpClient;

    private final String endpoint;

    public HttpUserSupport(OkHttpClient httpClient, String endpoint) {
        if (httpClient == null) {
            throw new IllegalArgumentException("httpClient must be defined.");
        }
        if (isBlank(endpoint)) {
            throw new IllegalArgumentException("endpoint must be defined.");
        }
        this.endpoint = endpoint.trim();
        this.httpClient = httpClient;
    }

    public HttpUserSupport(String endpoint) {
        this(new OkHttpClient(), endpoint);
    }

    public static Request.Builder addBasicAuthentification(UserInfo user, Request.Builder builder) {
        assert user != null : "user must be defined";
        assert builder != null : "builder must be defined";
        return addBasicAuthentification(user.getUsername(), user.getPassword(), builder);
    }

    public static Request.Builder addBasicAuthentification(String username, String password, Request.Builder builder) {
        assert StringUtils.isNotBlank(username) : "username must be defined";
        assert builder != null : "builder must be defined";

        String value = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        builder.addHeader("Authorization", value);
        return builder;
    }

    public static String generateProjectConfigurationDto(String projectName, UserInfo currentUser, StackConfigDto stackConfigDto) {
        String json = "{\n" +
                "   \"name\": \"" + projectName + "\",\n" +
                "   \"ownerIdentifier\": \"" + currentUser.getIdentifier() + "\"";
        if (stackConfigDto == null) {
            json += "\n";
        } else {

            json += ",\n   \"stackConfigs\": [\n" +
                    "    {\n" +
                    "      \"name\":\"" + stackConfigDto.getName() + "\",\n" +
                    "      \"type\":\"" + stackConfigDto.getType() + "\",\n" +
                    "      \"brickConfigs\": [\n";
            Iterator<BrickConfigDto> iterator = stackConfigDto.getBrickConfigs().iterator();
            while (iterator.hasNext()) {
                BrickConfigDto brickConfigDto = iterator.next();
                json += "        {\n" +
                        "          \"name\":\"" + brickConfigDto.getName() + "\",\n" +
                        "          \"type\":\"" + brickConfigDto.getType() + "\"\n" +
                        "        }";
                if (iterator.hasNext()) {
                    json += ",";
                }
                json += "\n";
            }
            json += "      ]\n" +
                    "    }\n" +
                    "  ]\n";
        }

        json += "}";
        return json;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getApiBaseUrl() {
        return "http://" + endpoint + "/api/v1";
    }

    public UserInfo createUser(UserInfo currentUser, String email) {

        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().post(emptyBody).url(getApiBaseUrl() + "/user");
        if (currentUser != null) {
            builder = addBasicAuthentification(currentUser, builder);
        }
        Request request = builder.build();
        Response response = null;
        String identifier = null;
        try {
            response = httpClient.newCall(request).execute();
            identifier = response.body().string();
            assertThat(identifier).isNotEmpty();
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), ("{\"email\": \"" + email + "\"}").getBytes());
        builder = new Request.Builder().post(body).url(getApiBaseUrl() + "/user/" + identifier);
        if (currentUser != null) {
            builder = addBasicAuthentification(currentUser, builder);
        }
        request = builder.build();
        response = null;
        try {
            response = httpClient.newCall(request).execute();
            JsonParser parser = new JsonParser();
            String bodyResponse = response.body().string();
            LOGGER.debug("Receive following user response :\n{}", bodyResponse);
            JsonObject json = (JsonObject) parser.parse(bodyResponse);
            String currentUsername = json.getAsJsonPrimitive("username").getAsString();
            String currentUserPassword = json.getAsJsonPrimitive("password").getAsString();
            String currentUserEmail = json.getAsJsonPrimitive("email").getAsString();
            String currentUserIdentifier = json.getAsJsonPrimitive("identifier").getAsString();
            String currentUserEntityIdentifier = json.getAsJsonPrimitive("entityIdentifier").getAsString();
            UserInfo res = new UserInfo(currentUsername, currentUserIdentifier, currentUserEntityIdentifier, currentUserPassword, currentUserEmail);
            return res;

        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return null;
    }

    public String generateHelloWebSocketMessage(UserInfo user) {
        String aggregateCredentials = String.format("%s:%s", user.getUsername(), user.getPassword());
        String encodedCredentials = Base64.getEncoder().encodeToString(aggregateCredentials.getBytes());
        return "{\n" +
                "  \"entity\": \"user\",\n" +
                "  \"action\": \"authentication\",\n" +
                "  \"data\": {\n" +
                "    \"authorization\": \"Basic " + encodedCredentials + "\"\n" +
                "  }\n" +
                "}";
    }


    public Session connectToWebSocketEvent(UserInfo user, WebSocketEventsListener listener) {
        final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.CREDENTIALS, new org.glassfish.tyrus.client.auth.Credentials(user.getUsername(), user.getPassword()));
        String uriStr = "ws://" + endpoint + "/api/v1/event";
        Session session = null;
        try {
            session = client.connectToServer(listener, cec, new URI(uriStr));
        } catch (DeploymentException | IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return session;
    }

    public Session connectToWebSocketEvent(UserInfo user, WebSocketEventsListener.CallBack callback) {
        return connectToWebSocketEvent(user, new WebSocketEventsListener(new WebSocketEventsCallbackWrapper(callback, user)));
    }

    public WebSocketConnectionResult connectToWebSocketAndWaitMessage(UserInfo user, CountDownLatch nbMessageExpected) {

        CountDownLatch webSocketConnected = new CountDownLatch(1);
        WebSocketEventsListener.CallBack delegate = new WebSocketEventsListener.CallBack() {
            @Override
            public void open(Session session) {
                webSocketConnected.countDown();
            }

            @Override
            public void receive(Session session, String message) {
                nbMessageExpected.countDown();
            }

            @Override
            public void close(Session session) {
                webSocketConnected.countDown();
            }
        };

        WebSocketEventsListener listener = new WebSocketEventsListener(new WebSocketEventsCallbackWrapper(delegate, user));
        Session session = connectToWebSocketEvent(user, listener);

        try {
            webSocketConnected.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //  NOTHING TO DO
        }

        return new WebSocketConnectionResult(session, listener);
    }

    public String createProjectConfiguration(String projectName, StackConfigDto stackConfigDto, UserInfo currentUser) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        String json = generateProjectConfigurationDto(projectName, currentUser, stackConfigDto);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + "/projectconfig").post(body);
        Request request = addBasicAuthentification(currentUser, builder).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            String payload = response.body().string();
            return payload;
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return null;
    }

    public void updateUser(UserInfo requester, UserInfo userChanged) {
        throw new UnsupportedOperationException("");
    }

    public enum UserChangeProjectConfig {
        ADD,
        REMOVE
    }


    public void addUserToProjectConfiguration(String projectConfigurationId, UserInfo currentUser, Iterator<UserInfo> userToAdds) {
        String json = generateUserIdJsonArray(userToAdds);
        Request.Builder builder = createChangeUserOnProjectConfiguration(getApiBaseUrl(), projectConfigurationId, json, UserChangeProjectConfig.ADD);

        Request request = addBasicAuthentification(currentUser, builder).build();
        executeRequestWithExpectedStatus(request, 200);
    }

    public void addUserToProjectConfiguration(String projectConfigurationId, UserInfo currentUser, UserInfo userToAdd) {
        addUserToProjectConfiguration(projectConfigurationId, currentUser, Collections.singletonList(userToAdd).iterator());
    }

    public void removeUserToProjectConfiguration(String projectConfigurationId, UserInfo currentUser, Iterator<UserInfo> userToRemoves) {
        String json = generateUserIdJsonArray(userToRemoves);
        Request.Builder builder = createChangeUserOnProjectConfiguration(getApiBaseUrl(), projectConfigurationId, json, UserChangeProjectConfig.REMOVE);
        Request request = addBasicAuthentification(currentUser, builder).build();
        executeRequestWithExpectedStatus(request, 200);
    }

    public void removeUserToProjectConfiguration(String projectConfigurationId, UserInfo currentUser, UserInfo userToRemove) {
        removeUserToProjectConfiguration(projectConfigurationId, currentUser, Collections.singletonList(userToRemove).iterator());
    }

    public ProjectDto getProjectDto(UserInfo currentUser, String projectId) {

        if (isBlank(projectId)) {
            throw new IllegalArgumentException("projectId must be defined.");
        }
        return getRessources("/project/" + projectId, currentUser, ProjectDto.class);
    }

    public UserDto getUserDto(UserInfo currentUser, String userId) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("projectId must be defined.");
        }
        return getRessources("/user/" + userId, currentUser, UserDto.class);
    }

    private <T extends Serializable> T getRessources(String path, UserInfo currentUser, Class<T> expectedType) {

        Request.Builder builder = new Request.Builder().url(getApiBaseUrl() + path).get();
        builder = addBasicAuthentification(currentUser, builder);
        Response response = null;
        try {
            response = httpClient.newCall(builder.build()).execute();
            assertThat(response.code()).isEqualTo(200);
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(response.body().string(), expectedType);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return null;
    }

    public void startProject(String projectConfigurationIdentifier, UserInfo currentUser) {
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().url("http://" + endpoint + "/api/v1/project/" + projectConfigurationIdentifier).post(body);
        builder = HttpUserSupport.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }


    private void executeRequestWithExpectedStatus(Request request, int expectedStatus) {

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(expectedStatus);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

    private static String generateUserIdJsonArray(Iterator<UserInfo> userToAdds) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        while (userToAdds.hasNext()) {
            UserInfo userToAdd = userToAdds.next();
            json.append("    \"").append(userToAdd.getIdentifier()).append("\"");
            if (userToAdds.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]");
        return json.toString();
    }

    private static Request.Builder createChangeUserOnProjectConfiguration(String apiBaseurl, String projectConfigurationId, String json, UserChangeProjectConfig userChangeProjectConfig) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(apiBaseurl + "/projectconfig/" + projectConfigurationId + "/user");
        if (userChangeProjectConfig == UserChangeProjectConfig.ADD) {
            builder = builder.put(body);
        } else {
            builder = builder.delete(body);
        }
        return builder;

    }


    private class WebSocketEventsCallbackWrapper implements WebSocketEventsListener.CallBack {

        private final WebSocketEventsListener.CallBack delegate;

        private final UserInfo user;

        private WebSocketEventsCallbackWrapper(WebSocketEventsListener.CallBack delegate, UserInfo user) {
            if (delegate == null) {
                throw new IllegalArgumentException("delegate must be defined.");
            }
            if (user == null) {
                throw new IllegalArgumentException("user must be defined.");
            }
            this.delegate = delegate;
            this.user = user;
        }

        @Override
        public void open(Session session) {
            try {
                session.getBasicRemote().sendText(generateHelloWebSocketMessage(user));
            } catch (IOException e) {
                fail(e.getMessage());
            }
            delegate.open(session);
        }

        @Override
        public void receive(Session session, String message) {
            delegate.receive(session, message);
        }

        @Override
        public void close(Session session) {
            delegate.close(session);
        }
    }

}
