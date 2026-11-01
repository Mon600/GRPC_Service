package app.exceptions;

public class InvalidUuidException extends RuntimeException {
    public InvalidUuidException(String message) {
        super(message);
    }
}
