package io.kodokojo.bdd.stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.Session;

@ClientEndpoint
public class WebSocketEventsListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEventsListener.class);

    @OnMessage
    public void receive(String message, Session session) {
        LOGGER.info(message);
    }
}
