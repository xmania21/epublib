package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.*;

/**
 * Roundtrip tests: write a {@link Book} to an EPUB file, read it back,
 * and assert structural equality of the most important fields.
 *
 * @since 5.0
 */
class EpubRoundtripTest {

    @Test
    void roundtrip_bookWithTitleAndAuthor_preservesMetadata(@TempDir Path tempDir) throws Exception {
        // Build source book
        Book original = Book.builder()
                .title("Roundtrip Test Book")
                .author(Author.of("Jane", "Doe"))
                .language("en")
                .addSection("Chapter 1",
                        Resource.fromBytes(
                                "<html><body><h1>Chapter 1</h1></body></html>".getBytes(),
                                "chapter1.html"))
                .build();

        // Write to file
        Path epubPath = tempDir.resolve("test.epub");
        try (FileOutputStream out = new FileOutputStream(epubPath.toFile())) {
            new EpubWriter().write(original, out);
        }

        // Read back
        Book result;
        try (ZipFile zip = new ZipFile(epubPath.toFile())) {
            result = new EpubReader().readEpub(zip);
        }

        assertThat(result.getTitle()).isEqualTo("Roundtrip Test Book");
        assertThat(result.getMetadata().getAuthors()).hasSize(1);
        assertThat(result.getMetadata().getAuthors().get(0).getLastname()).isEqualTo("Doe");
        assertThat(result.getMetadata().getLanguage()).isEqualTo("en");
    }

    @Test
    void roundtrip_bookWithMultipleSections_preservesSpineOrder(@TempDir Path tempDir) throws Exception {
        Resource ch1 = Resource.fromBytes("<html><body>Chapter 1</body></html>".getBytes(), "ch1.html");
        Resource ch2 = Resource.fromBytes("<html><body>Chapter 2</body></html>".getBytes(), "ch2.html");
        Resource ch3 = Resource.fromBytes("<html><body>Chapter 3</body></html>".getBytes(), "ch3.html");

        Book original = Book.builder()
                .title("Multi-Chapter Book")
                .author(Author.of("John", "Smith"))
                .addSection("Chapter 1", ch1)
                .addSection("Chapter 2", ch2)
                .addSection("Chapter 3", ch3)
                .build();

        Path epubPath = tempDir.resolve("multi.epub");
        try (FileOutputStream out = new FileOutputStream(epubPath.toFile())) {
            new EpubWriter().write(original, out);
        }

        Book result;
        try (ZipFile zip = new ZipFile(epubPath.toFile())) {
            result = new EpubReader().readEpub(zip);
        }

        assertThat(result.getSpine().size()).isEqualTo(3);
        assertThat(result.getTableOfContents().getTocReferences()).hasSize(3);
        assertThat(result.getTableOfContents().getTocReferences().get(0).getTitle()).isEqualTo("Chapter 1");
        assertThat(result.getTableOfContents().getTocReferences().get(1).getTitle()).isEqualTo("Chapter 2");
        assertThat(result.getTableOfContents().getTocReferences().get(2).getTitle()).isEqualTo("Chapter 3");
    }

    @Test
    void roundtrip_bookWithCssResource_resourceSurvivesRoundtrip(@TempDir Path tempDir) throws Exception {
        Resource css = Resource.fromBytes("body { font-family: serif; }".getBytes(), "book.css");

        Book original = Book.builder()
                .title("Styled Book")
                .author(Author.of("Alice", "Writer"))
                .addResource(css)
                .addSection("Chapter 1",
                        Resource.fromBytes("<html><head><link rel='stylesheet' href='book.css'/></head><body>Hello</body></html>".getBytes(),
                                "chapter1.html"))
                .build();

        Path epubPath = tempDir.resolve("styled.epub");
        try (FileOutputStream out = new FileOutputStream(epubPath.toFile())) {
            new EpubWriter().write(original, out);
        }

        Book result;
        try (ZipFile zip = new ZipFile(epubPath.toFile())) {
            result = new EpubReader().readEpub(zip);
        }

        assertThat(result.getResources().containsByHref("book.css")).isTrue();
    }

    @Test
    void roundtrip_emptyBook_doesNotThrow(@TempDir Path tempDir) throws Exception {
        Book original = Book.builder()
                .title("Empty Book")
                .build();

        Path epubPath = tempDir.resolve("empty.epub");
        try (FileOutputStream out = new FileOutputStream(epubPath.toFile())) {
            new EpubWriter().write(original, out);
        }

        assertThat(epubPath.toFile().exists()).isTrue();
        assertThat(epubPath.toFile().length()).isGreaterThan(0);
    }
}
