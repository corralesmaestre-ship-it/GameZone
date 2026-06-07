package exceptions;

public class VideoGameAlreadyExistsException extends Exception {
    public VideoGameAlreadyExistsException(String message) {
        super(message);
    }
}
