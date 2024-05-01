package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    ERROR("error"),
    JOIN_REQUEST("join-request"),
    JOIN_RESPONSE("join-response"),
    GAME_TYPE_SELECT_REQUEST("game-type-select-request"),
    GAME_TYPE_SELECT_RESPONSE("game-type-select-response"),
    GAME_STATE_UPDATE("game-state-update"),
    DISCARD_REQUEST("discard-request"),
    DISCARD_RESPONSE("discard-response"),
    OPTION_SELECT_REQUEST("option-select-request"),
    OPTION_SELECT_RESPONSE("option-select-response");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static MessageType fromValue(String messageTypeString) {
        for (MessageType type : MessageType.values()) {
            if (type.getValue().equals(messageTypeString)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown messageType, " + messageTypeString);
    }
}
