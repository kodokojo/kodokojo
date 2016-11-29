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
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.commons.endpoint.dto.WebSocketMessage;
import io.kodokojo.commons.endpoint.dto.WebSocketMessageGsonAdapter;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class BrickStateNotificationThen<SELF extends BrickStateNotificationThen<?>> extends Stage<SELF> {

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
            nbMessageExpected.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(WebSocketMessage.class, new WebSocketMessageGsonAdapter());
        Gson gson = builder.create();
        LinkedList<String> srcMessages = new LinkedList<>(listener.getMessages());
        assertThat(srcMessages).isNotEmpty();
        assertThat(srcMessages.size()).isGreaterThan(1);

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

        for (Map.Entry<String, List<WebSocketMessage>> entry : messagePerBrick.entrySet()) {
            List<WebSocketMessage> messages = entry.getValue();
            boolean configuring = false, starting = false, running = false;
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
