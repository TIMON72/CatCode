package app.classes.exceptions;

public class SyntaxException extends Exception {

    private static final long serialVersionUID = 1L;

    public SyntaxException(final String message) {
        super(message);
    }
}