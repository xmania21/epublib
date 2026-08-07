package nl.siegmann.epublib.exception;

/**
 * Exception thrown when an EPUB file cannot be written.
 *
 * <p>Wraps low-level I/O and serialization failures that occur while
 * writing an EPUB to an output stream or file.</p>
 *
 * @since 5.0
 */
public class EpubWriteException extends EpublibException {

    private static final long serialVersionUID = 1L;

    /** The href of the resource that triggered the write failure, or null if unknown. */
    private final String resourceHref;

    /**
     * Constructs an EpubWriteException with the given detail message.
     *
     * @param message the detail message
     */
    public EpubWriteException(String message) {
        super(message);
        this.resourceHref = null;
    }

    /**
     * Constructs an EpubWriteException with the given detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public EpubWriteException(String message, Throwable cause) {
        super(message, cause);
        this.resourceHref = null;
    }

    /**
     * Constructs an EpubWriteException with the given detail message, resource href and cause.
     *
     * @param message      the detail message
     * @param resourceHref the href of the resource that triggered the failure
     * @param cause        the cause
     */
    public EpubWriteException(String message, String resourceHref, Throwable cause) {
        super(message, cause);
        this.resourceHref = resourceHref;
    }

    /**
     * Returns the href of the resource that triggered the write failure.
     *
     * @return the resource href, or {@code null} if not available
     */
    public String getResourceHref() {
        return resourceHref;
    }
}
