package exceptions;

public class GeoDataException extends RuntimeException {
    public GeoDataException(String message) {
        super(message);
    }

    public GeoDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
