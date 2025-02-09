package exceptions;

public class OpenTripException extends RuntimeException {
    public OpenTripException(String message) {
        super(message);
    }

    public OpenTripException(String message, Throwable cause) {
        super(message, cause);
    }

}
