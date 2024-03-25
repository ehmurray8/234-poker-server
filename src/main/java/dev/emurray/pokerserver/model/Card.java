package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCard.class)
@JsonDeserialize(as = ImmutableCard.class)
public interface Card {
    Rank rank();

    Suit suit();
}
