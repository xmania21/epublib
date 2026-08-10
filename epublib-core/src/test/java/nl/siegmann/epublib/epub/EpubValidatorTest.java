package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.ValidationIssue;
import nl.siegmann.epublib.domain.ValidationReport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link EpubValidator}.
 *
 * @since 5.0
 */
class EpubValidatorTest {

    @Test
    void validate_fullyValidBook_returnsValidReport() {
        Book book = Book.builder()
                .title("Valid Book")
                .author(Author.of("Jane", "Doe"))
                .language("en")
                .identifier(Identifier.of(Identifier.Scheme.UUID, "test-uuid-123", true))
                .addSection("Chapter 1",
                        Resource.fromBytes("<html><body>Hello</body></html>".getBytes(), "ch1.html"))
                .build();

        ValidationReport report = EpubValidator.validate(book);

        assertThat(report.isValid()).isTrue();
        assertThat(report.getErrors()).isEmpty();
        assertThat(report.getBookTitle()).isEqualTo("Valid Book");
    }

    @Test
    void validate_emptySpine_returnsSpineError() {
        Book book = Book.builder()
                .title("No Chapters")
                .language("en")
                .author(Author.of("Jane", "Doe"))
                .identifier(Identifier.of(Identifier.Scheme.UUID, "uuid-456", true))
                .build();

        ValidationReport report = EpubValidator.validate(book);

        assertThat(report.isValid()).isFalse();
        assertThat(report.getErrors()).anyMatch(i -> i.getField().equals("spine"));
    }

    @Test
    void validate_missingTitle_returnsError() {
        Book book = new Book();
        book.getMetadata().setLanguage("en");

        ValidationReport report = EpubValidator.validate(book);

        assertThat(report.isValid()).isFalse();
        assertThat(report.getErrors()).anyMatch(i -> i.getField().equals("title"));
    }

    @Test
    void validate_noCoverImage_returnsWarning() {
        Book book = Book.builder()
                .title("No Cover")
                .language("en")
                .author(Author.of("Jane", "Doe"))
                .identifier(Identifier.of(Identifier.Scheme.UUID, "uuid-789", true))
                .addSection("Ch1", Resource.fromBytes("<html/>".getBytes(), "ch1.html"))
                .build();

        ValidationReport report = EpubValidator.validate(book);

        // No cover warning should be present, but book may still be valid (warnings don't fail)
        assertThat(report.getWarnings()).anyMatch(i -> i.getField().equals("cover"));
        assertThat(report.isValid()).isTrue();
    }

    @Test
    void validate_nullBook_throwsIllegalArgument() {
        assertThatThrownBy(() -> EpubValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validationReport_toString_containsSummary() {
        Book book = new Book();
        ValidationReport report = EpubValidator.validate(book);
        assertThat(report.toString()).contains("error").contains("warning");
    }

    @Test
    void validate_validBook_reportIsValidReturnsTrue() {
        Resource cover = Resource.fromBytes(new byte[]{1, 2, 3}, "cover.jpg");
        Book book = Book.builder()
                .title("Good Book")
                .language("en")
                .author(Author.of("Alice", "Writer"))
                .identifier(Identifier.of(Identifier.Scheme.ISBN, "978-0-00-000000-0", true))
                .addSection("Intro", Resource.fromBytes("<html/>".getBytes(), "intro.html"))
                .coverImage(cover)
                .build();

        ValidationReport report = EpubValidator.validate(book);
        assertThat(report.isValid()).isTrue();
        assertThat(report.toString()).contains("VALID");
    }
}
