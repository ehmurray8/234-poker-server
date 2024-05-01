package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OptionType {
    FOLD("fold"),
    CHECK("check"),
    CALL("call"),
    RAISE("raise"),
    BET("bet");

    private final String value;

    OptionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
