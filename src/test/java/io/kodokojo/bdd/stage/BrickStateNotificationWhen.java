package io.kodokojo.bdd.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.entrypoint.dto.ProjectCreationDto;
import io.kodokojo.model.User;
import org.apache.commons.io.IOUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class BrickStateNotificationWhen<SELF extends BrickStateNotificationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    String entryPointUrl;

    @ExpectedScenarioState
    User currentUser;

    @ProvidedScenarioState
    WebSocketEventsListener listener;

    @ProvidedScenarioState
    Session session;

    @ProvidedScenarioState
    String[] expectedBrickStarted;

    @ProvidedScenarioState
    CountDownLatch nbMessageExpected;

    @ProvidedScenarioState
    String projectConfigurationIdentifier;

    public SELF i_create_a_project_configuration_with_default_brick() {
        ProjectCreationDto projectCreationDto = new ProjectCreationDto("Acme", currentUser.getIdentifier(), Collections.singletonList(currentUser.getIdentifier()));

        expectedBrickStarted = new String[]{"haproxy", "jenkins", "nexus", "gitlab"};
        startProjectConfiguration(projectCreationDto, expectedBrickStarted);
        return self();
    }

    private void startProjectConfiguration(ProjectCreationDto projectCreationDto, String[] expectedBrickStarted) {
        final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.CREDENTIALS, new org.glassfish.tyrus.client.auth.Credentials(currentUser.getUsername(), currentUser.getPassword()));
        String uriStr = "ws://" + entryPointUrl + "/api/v1/event";
        CountDownLatch webSocketConnected = new CountDownLatch(1);
        nbMessageExpected = new CountDownLatch((expectedBrickStarted.length * 3)+1);
        listener = new WebSocketEventsListener(new WebSocketEventsListener.CallBack() {
            @Override
            public void open(Session session) {
                try {
                    session.getBasicRemote().sendText(StageUtils.generateHelloWebSocketMessage(currentUser));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
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
        });
        try {
            session = client.connectToServer(listener, cec, new URI(uriStr));
            webSocketConnected.await(10, TimeUnit.SECONDS);
        } catch (DeploymentException | IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(projectCreationDto);

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request.Builder builder = new Request.Builder().url("http://" + entryPointUrl + "/api/v1/projectconfig").post(body);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            projectConfigurationIdentifier = response.body().string();

        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

    public SELF i_start_the_project() {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder builder = new Request.Builder().url("http://" + entryPointUrl + "/api/v1/project/" + projectConfigurationIdentifier).post(body);
        builder = StageUtils.addBasicAuthentification(currentUser, builder);
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);
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
