package io.kodokojo.entrypoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.Launcher;
import io.kodokojo.model.User;
import io.kodokojo.service.UserManager;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@WebSocket
public class WebSocketEntrypoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEntrypoint.class);

    private static final String SUCCESS_REGISTRATION_MESSAGE = "{\n" +
            "  \"type\": \"userRegistered\",\n" +
            "  \"message\": \"You are successfully registered\"\n" +
            "}";

    private final Queue<Session> sessions;

    private final Map<String, Session> userConnectedSession;

    private final UserManager userManager;

    //  WebSocket is built by Spark but we are not able to get the instance :/ .
    //  See : https://github.com/perwendel/spark/pull/383
    public WebSocketEntrypoint() {
        super();
        sessions = new ConcurrentLinkedDeque<>();
        userConnectedSession = new ConcurrentHashMap<>();
        userManager = Launcher.INJECTOR.getInstance(UserManager.class);

    }

    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
    }


    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {

        LOGGER.debug("Receive following message: {}", message);
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(message);
        String type = json.getAsJsonPrimitive("type").getAsString();
        switch (type) {
            case "userConnection" :
                String userId = json.getAsJsonPrimitive("userId").getAsString();
                User user = userManager.getUserByIdentifier(userId);
                if (user != null) {
                    userConnectedSession.put(userId, session);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    session.getRemote().sendString(SUCCESS_REGISTRATION_MESSAGE);
                }
                break;
            default:
                LOGGER.debug("Unknown message type " + type);
        }
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
        String identifier = null;
        Iterator<Map.Entry<String, Session>> iterator = userConnectedSession.entrySet().iterator();
        while(iterator.hasNext() && identifier == null) {
            Map.Entry<String, Session> sessionEntry = iterator.next();
            if (sessionEntry.getValue().equals(session)) {
                identifier = sessionEntry.getKey();
            }
        }
        if (identifier != null) {
            userConnectedSession.remove(identifier);
        }

    }

}
