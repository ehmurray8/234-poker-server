package dev.emurray.pokerserver.model;

public enum ErrorDetailCode {
    MALFORMED_REQUEST("malformed");

    private final String value;

    ErrorDetailCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
