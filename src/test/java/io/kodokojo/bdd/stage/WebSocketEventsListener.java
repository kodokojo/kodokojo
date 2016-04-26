package io.kodokojo.bdd.stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.util.LinkedList;

public class WebSocketEventsListener extends Endpoint{

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEventsListener.class);

    interface CallBack {
        void open(Session session);
        void receive(Session session, String message);
        void close(Session session);
    }

    private final LinkedList<String> messages;

    private final CallBack callBack;

    public WebSocketEventsListener(CallBack callBack) {
        messages = new LinkedList<>();
        this.callBack = callBack;
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        LOGGER.debug("WebSocket Session opened");
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                messages.addLast(message);
                if (callBack != null) {
                    callBack.receive(session, message);
                }
            }
        });
        if (callBack != null) {
            callBack.open(session);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (callBack != null) {
            callBack.close(session);
        }
    }

    public LinkedList<String> getMessages() {
        return new LinkedList<>(messages);
    }
}
