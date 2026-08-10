package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link EpubStreamReader} and {@link EpubStreamWriter}.
 *
 * @since 5.0
 */
class EpubStreamReaderWriterTest {

    @Test
    void streaming_roundtrip_correctlyPreservesStructureAndData(@TempDir Path tempDir) throws Exception {
        Path epubPath = tempDir.resolve("stream-test.epub");

        // 1. Build the skeleton structure
        Book book = Book.builder()
                .title("Streaming Book")
                .author(Author.of("John", "Streamer"))
                .language("en")
                .identifier(Identifier.of(Identifier.Scheme.UUID, "streaming-uuid-999", true))
                .build();

        // Register resource markers in manifest (data payload is empty for skeleton builder)
        Resource chapter = Resource.fromBytes(new byte[0], "chapter1.html");
        book.getResources().add(chapter);
        book.getSpine().addSpineReference(new nl.siegmann.epublib.domain.SpineReference(chapter));

        Resource image = Resource.fromBytes(new byte[0], "images/logo.png");
        book.getResources().add(image);

        // 2. Stream structure and data out using EpubStreamWriter
        try (EpubStreamWriter writer = new EpubStreamWriter(new FileOutputStream(epubPath.toFile()))) {
            writer.writeBookStructure(book);

            // Stream actual content payloads dynamically from InputStreams
            byte[] htmlContent = "<html><body><h1>Streaming Chapter</h1></body></html>".getBytes();
            writer.writeResource(chapter, new ByteArrayInputStream(htmlContent));

            byte[] imageBytes = new byte[]{10, 20, 30, 40};
            writer.writeResource(image, new ByteArrayInputStream(imageBytes));
        }

        // 3. Read structure and data back lazily using EpubStreamReader
        try (EpubStreamReader reader = new EpubStreamReader(epubPath.toFile())) {
            Book result = reader.getBook();

            assertThat(result.getTitle()).isEqualTo("Streaming Book");
            assertThat(result.getMetadata().getAuthors().get(0).getLastname()).isEqualTo("Streamer");

            // Verify resources exist
            assertThat(result.getResources().containsByHref("chapter1.html")).isTrue();
            assertThat(result.getResources().containsByHref("images/logo.png")).isTrue();

            // Verify content is streaming correctly on-demand
            Resource readChapter = result.getResources().getByHref("chapter1.html");
            try (InputStream in = reader.openStream(readChapter)) {
                byte[] data = org.apache.commons.io.IOUtils.toByteArray(in);
                assertThat(new String(data)).contains("Streaming Chapter");
            }

            Resource readImage = result.getResources().getByHref("images/logo.png");
            try (InputStream in = reader.openStream(readImage)) {
                byte[] data = org.apache.commons.io.IOUtils.toByteArray(in);
                assertThat(data).isEqualTo(new byte[]{10, 20, 30, 40});
            }
        }
    }
}
