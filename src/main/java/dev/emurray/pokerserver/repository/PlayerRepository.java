package dev.emurray.pokerserver.repository;

import dev.emurray.pokerserver.model.ImmutablePlayer;
import dev.emurray.pokerserver.model.JoinRequest;
import dev.emurray.pokerserver.model.Player;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PlayerRepository {
    private final Map<String, Player> sessionIdToPlayer;

    public PlayerRepository() {
        sessionIdToPlayer = new HashMap<>();
    }

    public Player getOrCreate(String sessionId, JoinRequest joinRequest) {
        if (sessionIdToPlayer.containsKey(sessionId)) {
            return sessionIdToPlayer.get(sessionId);
        }
        return ImmutablePlayer.builder()
            .hand(List.of())
            .amountThisTurn(BigDecimal.ZERO)
            .balance(BigDecimal.valueOf(300L))
            .name(joinRequest.name())
            .avatar(joinRequest.avatar())
            .isSittingOut(true)
            .hasFolded(false)
            .playerId(UUID.randomUUID().toString())
            .build();
    }
}
