package io.kodokojo.bdd.stage;

import javax.websocket.Session;

public class WebSocketConnectionResult {

    private final Session session;

    private final WebSocketEventsListener listener;

    public WebSocketConnectionResult(Session session, WebSocketEventsListener listener) {
        if (session == null) {
            throw new IllegalArgumentException("session must be defined.");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        this.session = session;
        this.listener = listener;
    }

    public Session getSession() {
        return session;
    }

    public WebSocketEventsListener getListener() {
        return listener;
    }

}
