package nl.siegmann.epublib.exception;

/**
 * Exception thrown when an EPUB file cannot be read or parsed.
 *
 * <p>Wraps low-level I/O and XML parsing failures with context about
 * what resource or document was being read at the time of failure.</p>
 *
 * @since 5.0
 */
public class EpubReadException extends EpublibException {

    private static final long serialVersionUID = 1L;

    /** The href of the resource that triggered the read failure, or null if unknown. */
    private final String resourceHref;

    /**
     * Constructs an EpubReadException with the given detail message.
     *
     * @param message the detail message
     */
    public EpubReadException(String message) {
        super(message);
        this.resourceHref = null;
    }

    /**
     * Constructs an EpubReadException with the given detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public EpubReadException(String message, Throwable cause) {
        super(message, cause);
        this.resourceHref = null;
    }

    /**
     * Constructs an EpubReadException with the given detail message, resource href and cause.
     *
     * @param message      the detail message
     * @param resourceHref the href of the resource that triggered the failure
     * @param cause        the cause
     */
    public EpubReadException(String message, String resourceHref, Throwable cause) {
        super(message, cause);
        this.resourceHref = resourceHref;
    }

    /**
     * Returns the href of the resource that triggered the read failure.
     *
     * @return the resource href, or {@code null} if not available
     */
    public String getResourceHref() {
        return resourceHref;
    }
}
