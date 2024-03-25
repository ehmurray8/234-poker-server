package dev.emurray.pokerserver.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.emurray.pokerserver.exception.InvalidMessageException;
import dev.emurray.pokerserver.model.MessageType;
import dev.emurray.pokerserver.repository.SessionRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class PokerSocketHandler extends TextWebSocketHandler {

    public static final String SESSION_COOKIE_NAME = "234-poker-session";

    private final SessionRepository sessionRepository;

    private final ObjectMapper objectMapper;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final List<RequestHandler> handlers;

    public PokerSocketHandler(
        SessionRepository sessionRepository,
        ObjectMapper objectMapper,
        List<RequestHandler> requestHandlers,
        GlobalExceptionHandler globalExceptionHandler
    ) {
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.handlers = requestHandlers;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var sessionId = getSessionId(session.getHandshakeHeaders());
        if (sessionId.isPresent()) {
            if (sessionRepository.getSession(sessionId.get()).isEmpty()) {
                sessionRepository.addSession(sessionId.get(), session);
            }
        } else {
            // TODO: Better error handling here?
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var sessionId = getSessionId(session.getHandshakeHeaders());
        sessionId.ifPresent(sessionRepository::removeSession);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        String message = new String(textMessage.asBytes());
        MessageType messageType;
        String messageDetails;
        try {
            try {
                List<String> messageParts = objectMapper.readValue(message, new TypeReference<>() {
                });
                if (messageParts == null || messageParts.size() != 2) {
                    String numParts =
                        messageParts == null ? "0" : String.valueOf(messageParts.size());
                    throw new InvalidMessageException(
                        "Message must have 2 parts, sent message had, " + numParts + ", parts."
                    );
                }
                messageType = MessageType.fromValue(messageParts.get(0));
                messageDetails = messageParts.get(1);
            } catch (JsonProcessingException e) {
                throw new InvalidMessageException(
                    "Malformed request could not determine message type", e
                );
            }

            String sessionId = getSessionId(session.getHandshakeHeaders()).get();
            handlers.stream().filter(it -> it.canHandle(messageType))
                .forEach(it -> it.handle(sessionId, session, messageDetails));
        } catch (Exception e) {
            globalExceptionHandler.handleException(session, e);
        }
    }

    private Optional<String> getSessionId(HttpHeaders httpHeaders) {
        var cookieHeader = Optional.ofNullable(httpHeaders.get(HttpHeaders.COOKIE)).orElse(List.of());
        var sessionCookie = cookieHeader.stream()
            .flatMap(cookieStr -> Stream.of(cookieStr.split(";")))
            .filter(cookie -> cookie.trim().startsWith(SESSION_COOKIE_NAME + "="))
            .findFirst();
        return sessionCookie.map(it -> it.split("=")[1].trim());
    }
}
