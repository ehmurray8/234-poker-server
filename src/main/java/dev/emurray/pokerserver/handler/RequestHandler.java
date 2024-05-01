package dev.emurray.pokerserver.handler;

import dev.emurray.pokerserver.model.MessageType;
import javax.annotation.Nullable;
import org.springframework.web.socket.WebSocketSession;

public interface RequestHandler {

    boolean canHandle(MessageType messageType);

    void handle(String sessionId, @Nullable  WebSocketSession session, String messageDetails);
}
