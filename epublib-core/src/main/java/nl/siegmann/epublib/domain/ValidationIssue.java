package nl.siegmann.epublib.domain;

import java.io.Serializable;

/**
 * Represents a single validation issue found when validating a {@link Book} or its {@link Metadata}.
 *
 * <p>Issues are categorized by severity: {@link Severity#ERROR} for required-field violations
 * that make the EPUB non-conformant, and {@link Severity#WARNING} for best-practice violations.</p>
 *
 * @since 5.0
 */
public class ValidationIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The severity of a validation issue.
     */
    public enum Severity {
        /** A required element or field is missing or malformed. */
        ERROR,
        /** A best-practice recommendation is not followed. */
        WARNING
    }

    private final Severity severity;
    private final String field;
    private final String message;

    /**
     * Constructs a ValidationIssue.
     *
     * @param severity the severity of the issue
     * @param field    the metadata field or component that has the issue (e.g. {@code "title"})
     * @param message  a human-readable description of the issue
     */
    public ValidationIssue(Severity severity, String field, String message) {
        this.severity = severity;
        this.field = field;
        this.message = message;
    }

    /**
     * Returns the severity of this issue.
     *
     * @return the severity; never {@code null}
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns the name of the field or component that has the issue.
     *
     * @return the field name; may be {@code null} if not applicable
     */
    public String getField() {
        return field;
    }

    /**
     * Returns a human-readable description of the issue.
     *
     * @return the message; never {@code null}
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return severity + " [" + field + "]: " + message;
    }
}
