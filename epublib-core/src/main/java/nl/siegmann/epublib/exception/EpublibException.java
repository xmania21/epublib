package nl.siegmann.epublib.exception;

/**
 * Base unchecked exception for all epublib errors.
 *
 * <p>All exceptions thrown by epublib extend this class, allowing callers to
 * catch all epublib-specific errors with a single catch block if desired.</p>
 *
 * @since 5.0
 */
public class EpublibException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an EpublibException with the given detail message.
     *
     * @param message the detail message
     */
    public EpublibException(String message) {
        super(message);
    }

    /**
     * Constructs an EpublibException with the given detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public EpublibException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an EpublibException wrapping the given cause.
     *
     * @param cause the cause
     */
    public EpublibException(Throwable cause) {
        super(cause);
    }
}
