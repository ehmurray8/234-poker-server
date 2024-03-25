package dev.emurray.pokerserver.model;

public enum MessageType {
    ERROR("error"), JOIN_REQUEST("join-request"), JOIN_RESPONSE("join-response");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

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
