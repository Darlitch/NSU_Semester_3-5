package exceptions;

public class ViewModelException extends RuntimeException {
    public ViewModelException(String message) {
        super(message);
    }

    public ViewModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
