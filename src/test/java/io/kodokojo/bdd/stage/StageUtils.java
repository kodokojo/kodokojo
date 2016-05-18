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
package io.kodokojo.bdd.stage;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.entrypoint.dto.BrickConfigDto;
import io.kodokojo.entrypoint.dto.ProjectDto;
import io.kodokojo.entrypoint.dto.StackConfigDto;
import io.kodokojo.entrypoint.dto.UserDto;
import io.kodokojo.model.Entity;
import io.kodokojo.model.User;
import io.kodokojo.service.store.EntityStore;
import io.kodokojo.service.store.UserStore;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class StageUtils {

    private StageUtils() {
        //
    }

    public static Request.Builder addBasicAuthentification(User user, Request.Builder builder) {
        assert user != null : "user must be defined";
        assert builder != null : "builder must be defined";
        return addBasicAuthentification(user.getUsername(), user.getPassword(), builder);
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

    public static String generateHelloWebSocketMessage(User user) {
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

    public static String createProjectConfiguration(String apiBaseUrl, String projectName, StackConfigDto stackConfigDto, UserInfo currentUser) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }

        //  Send project configuration configuration

        String json = StageUtils.generateProjectConfigurationDto(projectName, currentUser, stackConfigDto);

        OkHttpClient httpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.getBytes());
        Request.Builder builder = new Request.Builder().url(apiBaseUrl + "/projectconfig").post(body);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
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

    public enum UserChangeProjectConfig {
        ADD,
        REMOVE
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

    public static void addUserToProjectConfiguration(String apiBaseurl, String projectConfigurationId, UserInfo currentUser, Iterator<UserInfo> userToAdds) {
        String json = generateUserIdJsonArray(userToAdds);
        Request.Builder builder = createChangeUserOnProjectConfiguration(apiBaseurl, projectConfigurationId, json, UserChangeProjectConfig.ADD);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
        executeRequestWithExpectedStatus(request, 200);
    }

    public static void addUserToProjectConfiguration(String apiBaseurl, String projectConfigurationId, UserInfo currentUser, UserInfo userToAdd) {
        addUserToProjectConfiguration(apiBaseurl, projectConfigurationId, currentUser, Collections.singletonList(userToAdd).iterator());
    }

    public static void removeUserToProjectConfiguration(String apiBaseurl, String projectConfigurationId, UserInfo currentUser, Iterator<UserInfo> userToRemoves) {
        String json = generateUserIdJsonArray(userToRemoves);
        Request.Builder builder = createChangeUserOnProjectConfiguration(apiBaseurl, projectConfigurationId, json, UserChangeProjectConfig.REMOVE);
        Request request = StageUtils.addBasicAuthentification(currentUser, builder).build();
        executeRequestWithExpectedStatus(request, 200);
    }

    public static void removeUserToProjectConfiguration(String apiBaseurl, String projectConfigurationId, UserInfo currentUser, UserInfo userToRemove) {
        removeUserToProjectConfiguration(apiBaseurl, projectConfigurationId, currentUser, Collections.singletonList(userToRemove).iterator());
    }

    public static ProjectDto getProjectDto(String apiBaseUrl, UserInfo currentUser, String projectId) {
        if (isBlank(apiBaseUrl)) {
            throw new IllegalArgumentException("apiBaseUrl must be defined.");
        }
        if (isBlank(projectId)) {
            throw new IllegalArgumentException("projectId must be defined.");
        }
        return getRessources(apiBaseUrl + "/project/" + projectId, currentUser, ProjectDto.class);
    }

    public static UserDto getUserDto(String apiBaseUrl, UserInfo currentUser, String userId) {
        if (isBlank(apiBaseUrl)) {
            throw new IllegalArgumentException("apiBaseUrl must be defined.");
        }
        if (isBlank(userId)) {
            throw new IllegalArgumentException("projectId must be defined.");
        }
        return getRessources(apiBaseUrl + "/user/" + userId, currentUser, UserDto.class);
    }

    private static <T extends Serializable> T getRessources(String url, UserInfo currentUser, Class<T> expectedType) {

        OkHttpClient httpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url).get();
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


    public static Service startDockerRedis(DockerTestSupport dockerTestSupport) {
        DockerClient dockerClient = dockerTestSupport.getDockerClient();

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(6379);
        portBinding.bind(exposedPort, Ports.Binding(null));

        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("redis:latest")
                .withExposedPorts(exposedPort)
                .withPortBindings(portBinding)
                .exec();
        dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
        dockerTestSupport.addContainerIdToClean(createContainerResponse.getId());

        String redisHost = dockerTestSupport.getServerIp();
        int redisPort = dockerTestSupport.getExposedPort(createContainerResponse.getId(), 6379);

        long end = System.currentTimeMillis() + 10000;
        boolean redisIsReady = false;
        while (!redisIsReady && (end - System.currentTimeMillis()) > 0) {
            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
            try (Jedis jedis = jedisPool.getResource()) {
                String resPing = jedis.ping();
                redisIsReady = "PONG".equals(resPing);
            } catch (JedisConnectionException e) {
                //  Silently ignore, Redis may not be available
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertThat(redisIsReady).isTrue();
        return new Service("redis", redisHost, redisPort);
    }

    public static User createUser(String username, UserStore userStore, EntityStore entityStore) {
        String identifier = userStore.generateId();
        String password = new BigInteger(130, new SecureRandom()).toString(32);
        User user = null;

        try {
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String email = username + "@kodokojo.io";
            String sshPublicKey = RSAUtils.encodePublicKey(publicKey, email);
            user = new User(identifier, username, username, email, password, sshPublicKey);
            String entityId = entityStore.addEntity(new Entity(username, user));
            boolean userAdded = userStore.addUser(new User(user.getIdentifier(), entityId, username, username, email, password, sshPublicKey));
            assertThat(userAdded).isTrue();

        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        return user;
    }

    public static Session connectToWebSocketEvent(String entryPointUrl, User user, WebSocketEventsListener listener) {
        final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.CREDENTIALS, new org.glassfish.tyrus.client.auth.Credentials(user.getUsername(), user.getPassword()));
        String uriStr = "ws://" + entryPointUrl + "/api/v1/event";
        Session session = null;
        try {
            session = client.connectToServer(listener, cec, new URI(uriStr));
        } catch (DeploymentException | IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return session;
    }

    public static Session connectToWebSocketEvent(String entryPointUrl, User user, WebSocketEventsListener.CallBack callback) {
        return connectToWebSocketEvent(entryPointUrl, user, new WebSocketEventsListener(new WebSocketEventsCallbackWrapper(callback, user)));
    }

    public static WebSocketConnectionResult connectToWebSocketAndWaitMessage(String entryPointUrl, User user, CountDownLatch nbMessageExpected) {

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
        Session session = connectToWebSocketEvent(entryPointUrl, user, listener);

        try {
            webSocketConnected.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //  NOTHING TO DO
        }

        return new WebSocketConnectionResult(session, listener);
    }

    static class WebSocketEventsCallbackWrapper implements WebSocketEventsListener.CallBack {

        private final WebSocketEventsListener.CallBack delegate;

        private final User user;

        WebSocketEventsCallbackWrapper(WebSocketEventsListener.CallBack delegate, User user) {
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
                session.getBasicRemote().sendText(StageUtils.generateHelloWebSocketMessage(user));
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


    private static void executeRequestWithExpectedStatus(Request request, int expectedStatus) {
        OkHttpClient httpClient = new OkHttpClient();
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

}
