package dev.emurray.pokerserver;

import static dev.emurray.pokerserver.config.ApplicationConfig.DISCONNECTED_SESSION_KEY;

import dev.emurray.pokerserver.model.MessageType;
import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.repository.SessionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Game {
    private final List<Player> waitingPlayers;

    private final List<Player> playersInGame;

    private final Queue<String> disconnectedSessions;

    private final SessionRepository sessionRepository;

    public Game(
        SessionRepository sessionRepository,
        @Qualifier(DISCONNECTED_SESSION_KEY) Queue<String> disconnectedSessions
    ) {
        waitingPlayers = new ArrayList<>();
        playersInGame = new ArrayList<>();
        this.disconnectedSessions = disconnectedSessions;
        this.sessionRepository = sessionRepository;
    }

    public void addPlayer(String sessionId, Player player) {
        disconnectedSessions.remove(sessionId);
        if (!playersInGame.contains(player) && !waitingPlayers.contains(player)) {
            waitingPlayers.add(player);
        }
        sessionRepository.sendMessage(sessionId, MessageType.JOIN_RESPONSE, player);
    }

    public void gameLoop() {
        // TODO: Implement gameLoop
        // Ask broke players to re-join - Ask them to re-join
        // Send any broke players that did not re-join back to join form
        // Remove any disconnected players
        // Check how many players are waiting room, and remove any CPU players as needed
        // Add players from waiting room to in-game
        // Fill the rest of the game with CPU players

        // Ask dealer for game choice
        // Blinds
        // Deal
        // preflop
        // turn
        // river

        // Pay winners
    }
}
