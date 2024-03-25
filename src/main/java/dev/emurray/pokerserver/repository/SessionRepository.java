package dev.emurray.pokerserver.repository;

import static dev.emurray.pokerserver.config.ApplicationConfig.DISCONNECTED_SESSION_KEY;

import dev.emurray.pokerserver.MessageSender;
import dev.emurray.pokerserver.model.MessageType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SessionRepository {

    private final Map<String, WebSocketSession> sessions;

    private final MessageSender messageSender;

    private final Queue<String> disconnectedSessions;

    public SessionRepository(
        MessageSender messageSender,
        @Qualifier(DISCONNECTED_SESSION_KEY) Queue<String> disconnectedSessions
    ) {
        sessions = new HashMap<>();
        this.messageSender = messageSender;
        this.disconnectedSessions = disconnectedSessions;
    }

    public Optional<WebSocketSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void removeSession(String sessionId) {
        disconnectedSessions.add(sessionId);
    }

    public <T> void sendMessage(String sessionId, MessageType messageType, T message) {
        if (sessions.containsKey(sessionId)) {
            messageSender.sendMessage(sessions.get(sessionId), messageType, message);
        }
    }
}
