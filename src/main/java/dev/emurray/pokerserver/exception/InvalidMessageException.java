package dev.emurray.pokerserver.exception;

public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(String message) {
        super(message);
    }

    public InvalidMessageException(String message, Exception e) {
        super(message, e);
    }
}
