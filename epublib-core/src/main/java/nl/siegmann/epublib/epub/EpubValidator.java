package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.ValidationIssue;
import nl.siegmann.epublib.domain.ValidationIssue.Severity;
import nl.siegmann.epublib.domain.ValidationReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates an EPUB {@link Book} against structural and metadata requirements.
 *
 * <p>Use {@link #validate(Book)} to obtain a {@link ValidationReport} containing
 * all discovered issues. A book is considered valid if the report contains no
 * {@link Severity#ERROR ERROR}-level issues.</p>
 *
 * <h2>Checks performed</h2>
 * <ul>
 *   <li>Metadata: title, identifier, language, author (via {@link nl.siegmann.epublib.domain.Metadata#validate()})</li>
 *   <li>Spine: at least one spine item present</li>
 *   <li>Spine integrity: every spine resource must exist in the manifest</li>
 *   <li>Cover: cover image media type is an image type</li>
 *   <li>Resources: no resource has a null or blank href</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>
 *     ValidationReport report = EpubValidator.validate(book);
 *     if (!report.isValid()) {
 *         report.getErrors().forEach(System.err::println);
 *     }
 * </pre>
 *
 * @since 5.0
 */
public final class EpubValidator {

    private EpubValidator() {
        // utility class
    }

    /**
     * Validates the given {@link Book} and returns a {@link ValidationReport}.
     *
     * @param book the book to validate; must not be {@code null}
     * @return a report containing all discovered issues
     * @throws IllegalArgumentException if {@code book} is {@code null}
     */
    public static ValidationReport validate(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null");
        }

        List<ValidationIssue> issues = new ArrayList<>();

        // 1. Metadata checks (delegated to Metadata.validate())
        issues.addAll(book.getMetadata().validate());

        // 2. Spine checks
        validateSpine(book, issues);

        // 3. Cover image check
        validateCoverImage(book, issues);

        // 4. Manifest resource integrity
        validateResources(book, issues);

        return new ValidationReport(book.getTitle(), issues);
    }

    // -------------------------------------------------------------------------

    private static void validateSpine(Book book, List<ValidationIssue> issues) {
        if (book.getSpine() == null || book.getSpine().size() == 0) {
            issues.add(new ValidationIssue(Severity.ERROR, "spine",
                    "The spine is empty — the EPUB has no reading order"));
            return;
        }

        // Verify every spine resource is in the manifest
        for (SpineReference ref : book.getSpine().getSpineReferences()) {
            Resource resource = ref.getResource();
            if (resource == null) {
                issues.add(new ValidationIssue(Severity.ERROR, "spine",
                        "Spine contains a reference with no associated resource"));
                continue;
            }
            String href = resource.getHref();
            if (!book.getResources().containsByHref(href)) {
                issues.add(new ValidationIssue(Severity.ERROR, "spine",
                        "Spine resource not found in manifest: " + href));
            }
        }
    }

    private static void validateCoverImage(Book book, List<ValidationIssue> issues) {
        Resource cover = book.getCoverImage();
        if (cover == null) {
            issues.add(new ValidationIssue(Severity.WARNING, "cover",
                    "No cover image specified — readers may display a placeholder"));
            return;
        }
        if (cover.getMediaType() == null) {
            issues.add(new ValidationIssue(Severity.WARNING, "cover",
                    "Cover image has no media type"));
        } else {
            String name = cover.getMediaType().getName();
            if (!name.startsWith("image/")) {
                issues.add(new ValidationIssue(Severity.ERROR, "cover",
                        "Cover image media type is not an image type: " + name));
            }
        }
    }

    private static void validateResources(Book book, List<ValidationIssue> issues) {
        if (book.getResources() == null) {
            return;
        }
        for (Resource resource : book.getResources().getAll()) {
            if (resource.getHref() == null || resource.getHref().trim().isEmpty()) {
                issues.add(new ValidationIssue(Severity.ERROR, "manifest",
                        "A resource has a null or blank href — it cannot be referenced in the EPUB"));
            }
        }
    }
}
