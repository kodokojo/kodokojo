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
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.entrypoint.dto.WebSocketMessage;
import io.kodokojo.entrypoint.dto.WebSocketMessageGsonAdapter;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.fail;

public class AccessRestWhen<SELF extends AccessRestWhen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessRestWhen.class);

    private static final String SUCCESS_REGISTRATION_MESSAGE = "{\n" +
            "  \"type\": \"userRegistered\",\n" +
            "  \"message\": \"You are successfully registered\"\n" +
            "}";

    private final OkHttpClient httpClient = new OkHttpClient();

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    @ExpectedScenarioState
    String whoAmI;

    @ExpectedScenarioState
    Map<String, UserInfo> currentUsers;

    @ProvidedScenarioState
    int responseHttpStatusCode;

    @ProvidedScenarioState
    String responseHttpStatusBody;

    @ProvidedScenarioState
    boolean receiveWebSocketWelcome = false;

    public SELF try_to_access_to_get_url_$(@Quoted String url) {
        return try_to_access_to_call_$_url_$("GET", url);
    }


    private SELF try_to_access_to_call_$_url_$(@Quoted String methodName, @Quoted String url) {

        Request.Builder builder = new Request.Builder().get().url(getBaseUrl() + url);
        if (whoAmI != null) {
            UserInfo requesterUserInfo = currentUsers.get(whoAmI);
            builder = StageUtils.addBasicAuthentification(requesterUserInfo, builder);
        }
        Request request = builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            responseHttpStatusCode = response.code();
            responseHttpStatusBody = response.body().string();
            response.body().close();
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.body().close();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        }
        return self();
    }

    public SELF try_to_access_to_events_websocket() {
        UserInfo userInfo = currentUsers.get(whoAmI);
        connectToWebSocket(userInfo, true);
        return self();
    }

    public SELF try_to_access_to_events_websocket_as_anonymous() {
        connectToWebSocket(null, false);
        return self();
    }

    private void connectToWebSocket(UserInfo requesterUserInfo, boolean expectSuccess) {

        try {

            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            if (requesterUserInfo != null) {
                client.getProperties().put(ClientProperties.CREDENTIALS, new Credentials(requesterUserInfo.getUsername(), requesterUserInfo.getPassword()));

            }

            String uriStr = "ws://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/event";
            CountDownLatch messageLatch = new CountDownLatch(1);
            Session session = client.connectToServer(new Endpoint() {
                /*
                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    LOGGER.info("Closing connection.");
                    messageLatch.countDown();
                    super.onClose(session, closeReason);
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    LOGGER.error("Error on session", thr);
                    messageLatch.countDown();
                    super.onError(session, thr);
                }
                */
                @Override
                public void onOpen(Session session, EndpointConfig config) {

                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String messsage) {
                            GsonBuilder builder = new GsonBuilder();
                            builder.registerTypeAdapter(WebSocketMessage.class, new WebSocketMessageGsonAdapter());
                            Gson gson = builder.create();
                            WebSocketMessage response = gson.fromJson(messsage, WebSocketMessage.class);
                            LOGGER.info("Receive WebSocket mesage : {}", response);
                            if ("user".equals(response.getEntity())
                                    && "authentication".equals(response.getAction())
                                    && response.getData().has("message") && ((JsonObject) response.getData()).getAsJsonPrimitive("message").getAsString().equals("success")
                                    ) {
                                receiveWebSocketWelcome = true;
                                messageLatch.countDown();
                            } else {
                                receiveWebSocketWelcome = false;
                            }
                        }


                    });
                    if (requesterUserInfo != null) {
                        try {
                            String aggregateCredentials = String.format("%s:%s", requesterUserInfo.getUsername(), requesterUserInfo.getPassword());
                            String encodedCredentials = Base64.getEncoder().encodeToString(aggregateCredentials.getBytes());
                            session.getBasicRemote().sendText("{\n" +
                                    "  \"entity\": \"user\",\n" +
                                    "  \"action\": \"authentication\",\n" +
                                    "  \"data\": {\n" +
                                    "    \"authorization\": \"Basic " + encodedCredentials + "\"\n" +
                                    "  }\n" +
                                    "}");
                        } catch (IOException e) {
                            fail(e.getMessage());
                        }
                    }
                }

            }, cec, new URI(uriStr));
            messageLatch.await(10, TimeUnit.SECONDS);
            session.close();
        } catch (Exception e) {
            if (expectSuccess) {
                fail(e.getMessage());
            } else {
                receiveWebSocketWelcome = false;
            }
        }
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }
}
