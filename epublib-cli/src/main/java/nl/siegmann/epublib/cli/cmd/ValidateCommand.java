package nl.siegmann.epublib.cli.cmd;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.ValidationIssue;
import nl.siegmann.epublib.domain.ValidationReport;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubValidator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

/**
 * {@code epublib validate} — validates an EPUB file and reports any issues.
 *
 * <p>Exit code is 0 if the EPUB is valid, 1 if there are errors,
 * and 2 if the file cannot be opened.</p>
 *
 * <p>Example:</p>
 * <pre>
 *     epublib validate my-book.epub
 *     epublib validate --strict my-book.epub
 * </pre>
 *
 * @since 5.0
 */
@Command(
        name = "validate",
        description = "Validate an EPUB file against specification requirements",
        mixinStandardHelpOptions = true
)
public class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the EPUB file to validate", paramLabel = "EPUB_FILE")
    private File epubFile;

    @Option(
            names = {"--strict", "-s"},
            description = "Treat warnings as errors",
            defaultValue = "false"
    )
    private boolean strict;

    @Option(
            names = {"--quiet", "-q"},
            description = "Only print issues, suppress summary line",
            defaultValue = "false"
    )
    private boolean quiet;

    @Override
    public Integer call() throws Exception {
        if (!epubFile.exists()) {
            System.err.println("ERROR: File not found: " + epubFile.getAbsolutePath());
            return 2;
        }

        Book book;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(epubFile))) {
            book = new EpubReader().readEpub(zip);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to open EPUB: " + e.getMessage());
            return 2;
        }

        ValidationReport report = EpubValidator.validate(book);
        List<ValidationIssue> issues = report.getIssues();

        boolean hasErrors = false;
        boolean hasWarnings = false;

        for (ValidationIssue issue : issues) {
            System.out.println(issue);
            if (issue.getSeverity() == ValidationIssue.Severity.ERROR) {
                hasErrors = true;
            } else {
                hasWarnings = true;
            }
        }

        if (!quiet) {
            if (issues.isEmpty()) {
                System.out.println("VALID: " + epubFile.getName());
            } else {
                System.out.printf("ISSUES in %s: %d error(s), %d warning(s)%n",
                        epubFile.getName(),
                        issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR).count(),
                        issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING).count());
            }
        }

        if (hasErrors) return 1;
        if (strict && hasWarnings) return 1;
        return 0;
    }
}
