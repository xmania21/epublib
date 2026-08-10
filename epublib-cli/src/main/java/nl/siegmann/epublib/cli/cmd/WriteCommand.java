package nl.siegmann.epublib.cli.cmd;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.ValidationIssue;
import nl.siegmann.epublib.epub.EpubWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * {@code epublib write} — creates an EPUB from a directory of HTML files.
 *
 * <p>Example:</p>
 * <pre>
 *     epublib write --out my-book.epub --title "My Book" --author "Jane Doe" ./html-dir
 * </pre>
 *
 * @since 5.0
 */
@Command(
        name = "write",
        description = "Create an EPUB from a directory of HTML/XHTML files",
        mixinStandardHelpOptions = true
)
public class WriteCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Directory containing HTML/XHTML source files", paramLabel = "SOURCE_DIR")
    private File sourceDir;

    @Option(
            names = {"--out", "-o"},
            description = "Output EPUB file path",
            required = true,
            paramLabel = "OUTPUT_FILE"
    )
    private File outputFile;

    @Option(
            names = {"--title", "-t"},
            description = "Book title",
            defaultValue = "Untitled",
            paramLabel = "TITLE"
    )
    private String title;

    @Option(
            names = {"--author", "-a"},
            description = "Author name in 'Firstname Lastname' format (repeatable)",
            paramLabel = "AUTHOR"
    )
    private List<String> authorNames;

    @Option(
            names = {"--language", "-l"},
            description = "Language code (BCP 47, e.g. 'en', 'fr')",
            defaultValue = "en",
            paramLabel = "LANG"
    )
    private String language;

    @Option(
            names = {"--isbn"},
            description = "ISBN identifier",
            paramLabel = "ISBN"
    )
    private String isbn;

    @Option(
            names = {"--validate"},
            description = "Validate metadata before writing and abort on errors",
            defaultValue = "false"
    )
    private boolean validate;

    @Override
    public Integer call() throws Exception {
        if (!sourceDir.isDirectory()) {
            System.err.println("ERROR: Source must be a directory: " + sourceDir.getAbsolutePath());
            return 1;
        }

        Book.Builder builder = Book.builder()
                .title(title)
                .language(language);

        if (isbn != null && !isbn.isBlank()) {
            builder.identifier(Identifier.of(Identifier.Scheme.ISBN, isbn, true));
        } else {
            builder.identifier(Identifier.of(Identifier.Scheme.UUID, UUID.randomUUID().toString(), true));
        }

        if (authorNames != null) {
            for (String name : authorNames) {
                String[] parts = name.trim().split("\\s+", 2);
                if (parts.length == 2) {
                    builder.author(Author.of(parts[0], parts[1]));
                } else {
                    builder.author(Author.of("", parts[0]));
                }
            }
        }

        // Add all HTML/XHTML files from sourceDir as spine items
        File[] htmlFiles = sourceDir.listFiles(f -> {
            String n = f.getName().toLowerCase();
            return f.isFile() && (n.endsWith(".html") || n.endsWith(".xhtml") || n.endsWith(".htm"));
        });

        if (htmlFiles == null || htmlFiles.length == 0) {
            System.err.println("WARNING: No HTML/XHTML files found in " + sourceDir.getAbsolutePath());
        } else {
            java.util.Arrays.sort(htmlFiles); // alphabetical order
            for (File htmlFile : htmlFiles) {
                byte[] data = java.nio.file.Files.readAllBytes(htmlFile.toPath());
                Resource resource = Resource.fromBytes(data, htmlFile.getName());
                String sectionTitle = htmlFile.getName().replaceFirst("\\.[^.]+$", "");
                builder.addSection(sectionTitle, resource);
            }
        }

        Book book = builder.build();

        if (validate) {
            List<ValidationIssue> issues = book.getMetadata().validate();
            boolean hasErrors = false;
            for (ValidationIssue issue : issues) {
                System.err.println(issue);
                if (issue.getSeverity() == ValidationIssue.Severity.ERROR) {
                    hasErrors = true;
                }
            }
            if (hasErrors) {
                System.err.println("Aborted due to validation errors.");
                return 1;
            }
        }

        outputFile.getParentFile();
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            new EpubWriter().write(book, out);
        }

        System.out.println("Written: " + outputFile.getAbsolutePath()
                + " (" + outputFile.length() + " bytes)");
        return 0;
    }
}
