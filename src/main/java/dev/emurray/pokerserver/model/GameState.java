package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.math.BigDecimal;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
@RecordBuilder
public record GameState(
    List<Player> table,
    List<Card> hand,
    int seatPosition,
    int numCards,
    int buttonPosition,
    int tableSize,
    BigDecimal bigBlindAmount,
    BigDecimal potAmount,
    List<Card> communityCards
) {}
