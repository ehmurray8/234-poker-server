package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.soabase.recordbuilder.core.RecordBuilder;
import javax.annotation.Nullable;
import org.springframework.web.socket.WebSocketSession;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
@RecordBuilder
public record SocketEvent(
    @Nullable
    WebSocketSession session,
    String sessionId,
    MessageType messageType,
    String messageDetails
) {}
