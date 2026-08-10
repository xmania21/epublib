package nl.siegmann.epublib.cli.cmd;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.ValidationIssue;
import nl.siegmann.epublib.epub.EpubReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

/**
 * {@code epublib read} — reads an EPUB and prints its metadata to stdout.
 *
 * <p>Example:</p>
 * <pre>
 *     epublib read --epub my-book.epub
 *     epublib read --epub my-book.epub --validate
 *     epublib read --epub my-book.epub --format json
 * </pre>
 *
 * @since 5.0
 */
@Command(
        name = "read",
        description = "Read an EPUB and print its metadata",
        mixinStandardHelpOptions = true
)
public class ReadCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the EPUB file", paramLabel = "EPUB_FILE")
    private File epubFile;

    @Option(
            names = {"--validate", "-v"},
            description = "Also validate the EPUB metadata and print any issues",
            defaultValue = "false"
    )
    private boolean validate;

    @Option(
            names = {"--format", "-f"},
            description = "Output format: text (default) or json",
            defaultValue = "text",
            paramLabel = "FORMAT"
    )
    private String format;

    @Override
    public Integer call() throws Exception {
        if (!epubFile.exists()) {
            System.err.println("ERROR: File not found: " + epubFile.getAbsolutePath());
            return 1;
        }

        Book book;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(epubFile))) {
            book = new EpubReader().readEpub(zip);
        }

        if ("json".equalsIgnoreCase(format)) {
            printJson(book);
        } else {
            printText(book);
        }

        if (validate) {
            List<ValidationIssue> issues = book.getMetadata().validate();
            if (issues.isEmpty()) {
                System.out.println("\nValidation: OK");
            } else {
                System.out.println("\nValidation issues:");
                for (ValidationIssue issue : issues) {
                    System.out.println("  " + issue);
                }
            }
        }

        return 0;
    }

    private void printText(Book book) {
        System.out.println("Title    : " + book.getTitle());
        System.out.println("Language : " + book.getMetadata().getLanguage());
        System.out.println("Spine    : " + book.getSpine().size() + " item(s)");
        System.out.println("Resources: " + book.getResources().size() + " item(s)");

        List<Author> authors = book.getMetadata().getAuthors();
        if (!authors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Authors  : ");
            for (int i = 0; i < authors.size(); i++) {
                if (i > 0) sb.append(", ");
                Author a = authors.get(i);
                sb.append(a.getFirstname()).append(" ").append(a.getLastname());
            }
            System.out.println(sb);
        }

        List<Identifier> identifiers = book.getMetadata().getIdentifiers();
        if (!identifiers.isEmpty()) {
            System.out.println("ISBN/UUID: " + identifiers.get(0).getValue());
        }

        List<String> publishers = book.getMetadata().getPublishers();
        if (!publishers.isEmpty()) {
            System.out.println("Publisher: " + publishers.get(0));
        }
    }

    private void printJson(Book book) {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"title\": ").append(jsonString(book.getTitle())).append(",\n");
        sb.append("  \"language\": ").append(jsonString(book.getMetadata().getLanguage())).append(",\n");
        sb.append("  \"spineItems\": ").append(book.getSpine().size()).append(",\n");
        sb.append("  \"resources\": ").append(book.getResources().size()).append(",\n");
        sb.append("  \"authors\": [");
        List<Author> authors = book.getMetadata().getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            if (i > 0) sb.append(", ");
            Author a = authors.get(i);
            sb.append(jsonString(a.getFirstname() + " " + a.getLastname()));
        }
        sb.append("],\n");
        List<Identifier> ids = book.getMetadata().getIdentifiers();
        sb.append("  \"identifier\": ").append(ids.isEmpty() ? "null" : jsonString(ids.get(0).getValue())).append("\n");
        sb.append("}");
        System.out.println(sb);
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
