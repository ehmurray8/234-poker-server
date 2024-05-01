package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
@RecordBuilder
public record Player(
    String avatar,
    String name,
    BigDecimal balance,
    boolean hasFolded,
    BigDecimal amountThisTurn,
    boolean isSittingOut,
    String playerId,
    boolean isCpu
) implements PlayerBuilder.With {
    @Override
    public boolean equals(Object object) {
        if (object instanceof Player player) {
            return player.playerId().equals(playerId());
        }
        return false;
    }
}
