package dev.emurray.pokerserver.handler;

import dev.emurray.pokerserver.MessageSender;
import dev.emurray.pokerserver.exception.InvalidMessageException;
import dev.emurray.pokerserver.model.ErrorDetailCode;
import dev.emurray.pokerserver.model.ErrorResponse;
import dev.emurray.pokerserver.model.ImmutableErrorResponse;
import dev.emurray.pokerserver.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSender messageSender;

    public GlobalExceptionHandler(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void handleException(WebSocketSession session, Exception e) {
        if (e instanceof InvalidMessageException invalidMessageException) {
            log.error("Invalid message.", invalidMessageException);
            sendMalformedRequestMessage(session, e.getMessage());
        } else {
            log.error("Server error.", e);
            sendServerException(session);
        }
    }

    private void sendMalformedRequestMessage(WebSocketSession session, String message) {
        ErrorResponse errorResponse = ImmutableErrorResponse.builder()
            .message(message)
            .detailCode(ErrorDetailCode.MALFORMED_REQUEST.getValue())
            .code(HttpStatus.BAD_REQUEST.value())
            .build();
        messageSender.sendMessage(session, MessageType.ERROR, errorResponse);
    }

    private void sendServerException(WebSocketSession session) {
        ErrorResponse errorResponse = ImmutableErrorResponse.builder()
            .message("Internal Server Error")
            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();
        messageSender.sendMessage(session, MessageType.ERROR, errorResponse);
    }
}
