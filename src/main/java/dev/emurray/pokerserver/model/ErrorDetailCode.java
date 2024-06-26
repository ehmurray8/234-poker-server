package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorDetailCode {
    MALFORMED_REQUEST("malformed");

    private final String value;

    ErrorDetailCode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
