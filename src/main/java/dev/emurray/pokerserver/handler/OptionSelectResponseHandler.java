package dev.emurray.pokerserver.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.emurray.pokerserver.Game;
import dev.emurray.pokerserver.exception.InvalidMessageException;
import dev.emurray.pokerserver.model.MessageType;
import dev.emurray.pokerserver.model.OptionSelectResponse;
import dev.emurray.pokerserver.repository.PlayerRepository;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class OptionSelectResponseHandler implements RequestHandler {

    private static final Logger log = LoggerFactory.getLogger(GameTypeSelectResponseHandler.class);

    private final ObjectMapper objectMapper;

    private final Game game;

    private final PlayerRepository playerRepository;

    public OptionSelectResponseHandler(
        ObjectMapper objectMapper, Game game, PlayerRepository playerRepository
    ) {
        this.objectMapper = objectMapper;
        this.game = game;
        this.playerRepository = playerRepository;
    }

    @Override
    public boolean canHandle(MessageType messageType) {
        return MessageType.OPTION_SELECT_RESPONSE.equals(messageType);
    }

    @Override
    public void handle(
        String sessionId,
        @Nullable WebSocketSession session,
        String messageDetails
    ) {
        OptionSelectResponse optionSelectResponse;
        try {
            optionSelectResponse = objectMapper.readValue(messageDetails, OptionSelectResponse.class);
        } catch (JsonProcessingException e) {
            throw new InvalidMessageException("Failed to parse join request message");
        }
        var player = playerRepository.get(sessionId);
        if (player.isPresent()) {
            var option = optionSelectResponse.selectedOption();
            game.selectOption(option, player.get().playerId());
        } else {
            log.error("No player for session, {}", sessionId);
        }
    }
}
