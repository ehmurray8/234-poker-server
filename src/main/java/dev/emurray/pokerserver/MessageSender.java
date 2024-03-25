package dev.emurray.pokerserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.emurray.pokerserver.model.MessageType;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final ObjectMapper objectMapper;

    public MessageSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> void sendMessage(
        WebSocketSession session,
        MessageType messageType,
        T message
    ) {
        try {
            session.sendMessage(new TextMessage(writeValueAsString(
                List.of(messageType.getValue(), writeValueAsString(message))
            )));
        } catch (IOException e) {
            log.error("Failed to send message.", e);
        }
    }

    private <T> String writeValueAsString(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize", e);
        }
    }
}
