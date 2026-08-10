package nl.siegmann.epublib.cli.cmd;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

/**
 * {@code epublib convert} — reads an EPUB and re-writes it (useful for re-packing
 * or normalizing an EPUB through the epublib round-trip pipeline).
 *
 * <p>Example:</p>
 * <pre>
 *     epublib convert input.epub --out normalized.epub
 * </pre>
 *
 * <p>Future extensions may include applying book processors (XSL transforms,
 * cover injection, etc.) via {@code --processor} flags.</p>
 *
 * @since 5.0
 */
@Command(
        name = "convert",
        description = "Read an EPUB and re-write it (normalize/re-pack through epublib pipeline)",
        mixinStandardHelpOptions = true
)
public class ConvertCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Input EPUB file", paramLabel = "INPUT_FILE")
    private File inputFile;

    @Option(
            names = {"--out", "-o"},
            description = "Output EPUB file path",
            required = true,
            paramLabel = "OUTPUT_FILE"
    )
    private File outputFile;

    @Option(
            names = {"--overwrite", "-f"},
            description = "Overwrite output file if it already exists",
            defaultValue = "false"
    )
    private boolean overwrite;

    @Override
    public Integer call() throws Exception {
        if (!inputFile.exists()) {
            System.err.println("ERROR: Input file not found: " + inputFile.getAbsolutePath());
            return 1;
        }
        if (outputFile.exists() && !overwrite) {
            System.err.println("ERROR: Output file already exists (use --overwrite to force): "
                    + outputFile.getAbsolutePath());
            return 1;
        }

        System.out.println("Reading: " + inputFile.getAbsolutePath());
        Book book;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(inputFile))) {
            book = new EpubReader().readEpub(zip);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read EPUB: " + e.getMessage());
            return 1;
        }

        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        System.out.println("Writing: " + outputFile.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            new EpubWriter().write(book, out);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to write EPUB: " + e.getMessage());
            return 1;
        }

        System.out.printf("Done: %s (%.1f KB)%n",
                outputFile.getName(),
                outputFile.length() / 1024.0);
        return 0;
    }
}
