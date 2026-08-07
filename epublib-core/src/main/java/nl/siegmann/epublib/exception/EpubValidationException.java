package nl.siegmann.epublib.exception;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when an EPUB fails validation against the EPUB specification.
 *
 * <p>May carry a list of {@link ValidationIssue} objects describing all
 * individual violations found during validation.</p>
 *
 * @see nl.siegmann.epublib.domain.ValidationIssue
 * @since 5.0
 */
public class EpubValidationException extends EpublibException {

    private static final long serialVersionUID = 1L;

    private final List<String> violations;

    /**
     * Constructs an EpubValidationException with a single message.
     *
     * @param message the detail message
     */
    public EpubValidationException(String message) {
        super(message);
        this.violations = Collections.emptyList();
    }

    /**
     * Constructs an EpubValidationException with a message and a list of violation strings.
     *
     * @param message    the summary detail message
     * @param violations the individual violations found
     */
    public EpubValidationException(String message, List<String> violations) {
        super(message);
        this.violations = violations == null ? Collections.emptyList() : Collections.unmodifiableList(violations);
    }

    /**
     * Constructs an EpubValidationException wrapping an underlying cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public EpubValidationException(String message, Throwable cause) {
        super(message, cause);
        this.violations = Collections.emptyList();
    }

    /**
     * Returns the list of individual violation strings found during validation.
     *
     * @return an unmodifiable list of violation strings; never {@code null}
     */
    public List<String> getViolations() {
        return violations;
    }
}
