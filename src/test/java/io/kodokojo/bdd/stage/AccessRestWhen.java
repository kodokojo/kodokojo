package io.kodokojo.bdd.stage;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.auth.AuthConfig;
import org.glassfish.tyrus.client.auth.AuthenticationException;
import org.glassfish.tyrus.client.auth.Authenticator;
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
    boolean receiveWebSocketWelcome;

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
        UserInfo requesterUserInfo = currentUsers.get(whoAmI);

        try {

            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            client.getProperties().put(ClientProperties.CREDENTIALS, new Credentials(requesterUserInfo.getUsername(), requesterUserInfo.getPassword()));

            String uriStr = "ws://" + restEntryPointHost + ":" + restEntryPointPort + "/api/v1/event";
            CountDownLatch messageLatch = new CountDownLatch(1);
            Session session = client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {

                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String messsage) {
                            if (messsage.equals(SUCCESS_REGISTRATION_MESSAGE)) {
                                receiveWebSocketWelcome = true;
                            }
                            messageLatch.countDown();
                        }

                    });

                    try {
                        session.getBasicRemote().sendText("{\n" +
                                "  \"type\": \"userConnection\",\n" +
                                "  \"userId\": \"" + requesterUserInfo.getIdentifier() + "\"\n" +
                                "}");
                    } catch (IOException e) {
                        fail(e.getMessage());
                    }
                }
            }, cec, new URI(uriStr));
            messageLatch.await(100, TimeUnit.SECONDS);
            session.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return self();
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }

}
