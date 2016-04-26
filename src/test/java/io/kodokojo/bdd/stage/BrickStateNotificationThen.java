package io.kodokojo.bdd.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.entrypoint.dto.WebSocketMessage;
import io.kodokojo.entrypoint.dto.WebSocketMessageGsonAdapter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class BrickStateNotificationThen<SELF extends BrickStateNotificationThen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickStateNotificationThen.class);

    @ExpectedScenarioState
    WebSocketEventsListener listener;

    @ExpectedScenarioState
    String[] expectedBrickStarted;

    @ExpectedScenarioState
    CountDownLatch nbMessageExpected;

    @ExpectedScenarioState
    CurrentStep currentStep;

    public SELF i_receive_all_notification() {
        try {
            nbMessageExpected.await(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(WebSocketMessage.class, new WebSocketMessageGsonAdapter());
        Gson gson = builder.create();
        LinkedList<String> srcMessages = new LinkedList<>(listener.getMessages());
        List<WebSocketMessage> webSocketMessages = srcMessages.stream().map(m -> gson.fromJson(m, WebSocketMessage.class)).collect(Collectors.toList());
        Map<String, List<WebSocketMessage>> messagePerBrick = new HashMap<>();
        for (WebSocketMessage message : webSocketMessages) {
            if ("brick".equals(message.getEntity())) {
                assertThat(message.getEntity()).isEqualTo("brick");
                assertThat(message.getAction()).isEqualTo("updateState");
                assertThat(message.getData().has("projectConfiguration")).isTrue();
                assertThat(message.getData().has("brickType")).isTrue();
                assertThat(message.getData().has("brickName")).isTrue();
                assertThat(message.getData().has("state")).isTrue();

                String brickName = message.getData().get("brickName").getAsString();
                assertThat(brickName).isIn(expectedBrickStarted);
                List<WebSocketMessage> previous = messagePerBrick.get(brickName);
                if (previous != null) {
                } else {
                    previous = new ArrayList<>();
                    messagePerBrick.put(brickName, previous);
                }
                previous.add(message);
            }
        }

        for(Map.Entry<String, List<WebSocketMessage>> entry : messagePerBrick.entrySet()) {
            List<WebSocketMessage> messages = entry.getValue();
            boolean configuring = false , starting = false , running = false;
            for (WebSocketMessage webSocketMessage : messages) {
                String state = webSocketMessage.getData().get("state").getAsString();
                switch (state) {
                    case "STARTING":
                        starting = true;
                        break;
                    case "CONFIGURING":
                        configuring = true;
                        break;
                    case "RUNNING":
                        running = true;
                        break;
                    default:
                        fail("UnExpected state " + state);
                }
            }
            assertThat(configuring).isTrue();
            assertThat(starting).isTrue();
            assertThat(running).isTrue();
        }

        currentStep.addAttachment(Attachment.plainText(StringUtils.join(srcMessages, "\n")).withTitle("WebSocket messages receive").withFileName("websocketMessages"));

        return self();
    }
}
