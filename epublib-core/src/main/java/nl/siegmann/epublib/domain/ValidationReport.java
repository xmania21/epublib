package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates the results of validating a {@link Book}.
 *
 * <p>Produced by {@link nl.siegmann.epublib.epub.EpubValidator#validate(Book)}.
 * A report is considered <em>valid</em> if it contains no {@link ValidationIssue.Severity#ERROR}
 * issues (warnings are allowed).</p>
 *
 * <pre>
 *     ValidationReport report = EpubValidator.validate(book);
 *     if (report.isValid()) {
 *         System.out.println("OK: " + report.getBookTitle());
 *     } else {
 *         report.getErrors().forEach(e -> System.err.println(e));
 *     }
 * </pre>
 *
 * @since 5.0
 */
public class ValidationReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String bookTitle;
    private final List<ValidationIssue> issues;

    /**
     * Constructs a ValidationReport.
     *
     * @param bookTitle the title of the validated book, or {@code null} if not set
     * @param issues    the list of issues found; may be empty
     */
    public ValidationReport(String bookTitle, List<ValidationIssue> issues) {
        this.bookTitle = bookTitle;
        this.issues = issues == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(issues));
    }

    /**
     * Returns the title of the book that was validated.
     *
     * @return the book title, or {@code null}
     */
    public String getBookTitle() {
        return bookTitle;
    }

    /**
     * Returns all issues found, including both errors and warnings.
     *
     * @return an unmodifiable list of issues; never {@code null}
     */
    public List<ValidationIssue> getIssues() {
        return issues;
    }

    /**
     * Returns only the {@link ValidationIssue.Severity#ERROR ERROR}-level issues.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<ValidationIssue> getErrors() {
        List<ValidationIssue> errors = new ArrayList<>();
        for (ValidationIssue issue : issues) {
            if (issue.getSeverity() == ValidationIssue.Severity.ERROR) {
                errors.add(issue);
            }
        }
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns only the {@link ValidationIssue.Severity#WARNING WARNING}-level issues.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<ValidationIssue> getWarnings() {
        List<ValidationIssue> warnings = new ArrayList<>();
        for (ValidationIssue issue : issues) {
            if (issue.getSeverity() == ValidationIssue.Severity.WARNING) {
                warnings.add(issue);
            }
        }
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Returns {@code true} if this report contains no {@link ValidationIssue.Severity#ERROR} issues.
     *
     * @return {@code true} if the book is structurally valid (warnings are allowed)
     */
    public boolean isValid() {
        for (ValidationIssue issue : issues) {
            if (issue.getSeverity() == ValidationIssue.Severity.ERROR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a human-readable summary of this report.
     *
     * @return summary string
     */
    @Override
    public String toString() {
        if (issues.isEmpty()) {
            return "ValidationReport[" + bookTitle + "]: VALID";
        }
        return "ValidationReport[" + bookTitle + "]: "
                + getErrors().size() + " error(s), "
                + getWarnings().size() + " warning(s)";
    }
}
