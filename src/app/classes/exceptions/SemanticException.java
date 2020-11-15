package app.classes.exceptions;

public class SemanticException extends Exception {

    private static final long serialVersionUID = 1L;

    public SemanticException(final String message) {
        super(message);
    }
}