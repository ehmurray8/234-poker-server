package dev.emurray.pokerserver.repository;

import dev.emurray.pokerserver.model.JoinRequest;
import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.model.PlayerBuilder;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PlayerRepository {
    private final Map<String, Player> sessionIdToPlayer;
    private final Map<String, String> playerIdToSessionId;

    public PlayerRepository() {
        sessionIdToPlayer = new HashMap<>();
        playerIdToSessionId = new HashMap<>();
    }

    public Player getOrCreate(String sessionId, JoinRequest joinRequest) {
        if (sessionIdToPlayer.containsKey(sessionId)) {
            return sessionIdToPlayer.get(sessionId);
        }
        var player = PlayerBuilder.builder()
            .amountThisTurn(BigDecimal.ZERO)
            .balance(BigDecimal.valueOf(300L))
            .name(joinRequest.name())
            .avatar(joinRequest.avatar())
            .isSittingOut(true)
            .hasFolded(false)
            .playerId(UUID.randomUUID().toString())
            .build();
        sessionIdToPlayer.put(sessionId, player);
        playerIdToSessionId.put(player.playerId(), sessionId);
        return player;
    }

    public Player createCpuPlayer(int seatPosition) {
        String sessionId = "cpu-session-" + seatPosition;
        var player = PlayerBuilder.builder()
            .amountThisTurn(BigDecimal.ZERO)
            .balance(BigDecimal.valueOf(300L))
            .name("CPU " + seatPosition)
            .avatar("/images/default-user.png")
            .isSittingOut(true)
            .hasFolded(false)
            .playerId(UUID.randomUUID().toString())
            .isCpu(true)
            .build();
        sessionIdToPlayer.put(sessionId, player);
        playerIdToSessionId.put(player.playerId(), sessionId);
        return player;
    }

    public Optional<Player> get(String sessionId) {
        return Optional.ofNullable(sessionIdToPlayer.get(sessionId));
    }

    public Optional<String> getSessionId(String playerId) {
        return Optional.ofNullable(playerIdToSessionId.get(playerId));
    }

    public void removePlayer(String playerId) {
        if (playerId != null) {
            var sessionId = playerIdToSessionId.get(playerId);
            playerIdToSessionId.remove(playerId);
            if (sessionId != null) {
                sessionIdToPlayer.remove(sessionId);
            }
        }
    }
}
