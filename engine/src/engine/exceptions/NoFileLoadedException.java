package engine.exceptions;

public class NoFileLoadedException extends RuntimeException {
    public NoFileLoadedException(String message) {
        super(message);
    }
}