package dev.emurray.pokerserver.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.emurray.pokerserver.Game;
import dev.emurray.pokerserver.exception.InvalidMessageException;
import dev.emurray.pokerserver.model.JoinRequest;
import dev.emurray.pokerserver.model.MessageType;
import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.repository.PlayerRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class JoinRequestHandler implements RequestHandler {


    private final ObjectMapper objectMapper;

    private final PlayerRepository playerRepository;

    private final Game game;

    public JoinRequestHandler(
        ObjectMapper objectMapper,
        PlayerRepository playerRepository,
        Game game
    ) {
        this.objectMapper = objectMapper;
        this.playerRepository = playerRepository;
        this.game = game;
    }

    @Override
    public boolean canHandle(MessageType messageType) {
        return MessageType.JOIN_REQUEST.equals(messageType);
    }

    @Override
    public void handle(String sessionId, WebSocketSession session, String messageDetails) {
        JoinRequest joinRequest;
        try {
            joinRequest = objectMapper.readValue(messageDetails, JoinRequest.class);
        } catch (JsonProcessingException e) {
            throw new InvalidMessageException("Failed to parse join request message");
        }

        Player player = playerRepository.getOrCreate(sessionId, joinRequest);
        game.addPlayer(sessionId, player);
    }
}
