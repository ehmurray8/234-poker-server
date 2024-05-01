package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum GameType {
    HOLD_EM("hold-em", 2),
    PINEAPPLE("pineapple", 3),
    OMAHA("omaha", 4);

    private final String value;

    private final int numCards;

    GameType(String value, int numCards) {
        this.value = value;
        this.numCards = numCards;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public int getNumCards() {
        return numCards;
    }

    public static Optional<GameType> fromValue(String value) {
        for (var type : values()) {
            if (type.getValue().equals(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
