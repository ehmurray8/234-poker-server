package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePlayer.class)
@JsonDeserialize(as = ImmutablePlayer.class)
public abstract class Player {
    public abstract String avatar();

    public abstract String name();

    public abstract BigDecimal balance();

    public abstract List<Card> hand();

    public abstract boolean hasFolded();

    public abstract BigDecimal amountThisTurn();

    public abstract boolean isSittingOut();

    public abstract String playerId();

    @Override
    public boolean equals(Object object) {
        if (object instanceof Player player) {
            return player.playerId().equals(playerId());
        }
        return false;
    }
}
