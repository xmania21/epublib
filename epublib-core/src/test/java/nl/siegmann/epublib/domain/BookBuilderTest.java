package nl.siegmann.epublib.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link Book.Builder} fluent API.
 *
 * @since 5.0
 */
class BookBuilderTest {

    @Test
    void builder_withTitle_setsTitleInMetadata() {
        Book book = Book.builder()
                .title("My Test Book")
                .build();

        assertThat(book.getTitle()).isEqualTo("My Test Book");
    }

    @Test
    void builder_withAuthor_setsAuthorInMetadata() {
        Author author = Author.of("Jane", "Doe");

        Book book = Book.builder()
                .title("Test Book")
                .author(author)
                .build();

        assertThat(book.getMetadata().getAuthors()).containsExactly(author);
    }

    @Test
    void builder_withMultipleAuthors_allPresent() {
        Author author1 = Author.of("Jane", "Doe");
        Author author2 = Author.of("John", "Smith");

        Book book = Book.builder()
                .author(author1)
                .author(author2)
                .build();

        assertThat(book.getMetadata().getAuthors()).containsExactly(author1, author2);
    }

    @Test
    void builder_withLanguage_setsLanguageInMetadata() {
        Book book = Book.builder()
                .language("fr")
                .build();

        assertThat(book.getMetadata().getLanguage()).isEqualTo("fr");
    }

    @Test
    void builder_withIdentifier_setsIdentifier() {
        Identifier id = Identifier.of(Identifier.Scheme.ISBN, "978-3-16-148410-0", true);

        Book book = Book.builder()
                .identifier(id)
                .build();

        assertThat(book.getMetadata().getIdentifiers()).contains(id);
    }

    @Test
    void builder_withSection_addsToSpineAndToc() {
        Resource chapter = Resource.fromBytes("<html><body>Hello</body></html>".getBytes(), "chapter1.html");

        Book book = Book.builder()
                .title("Test Book")
                .addSection("Chapter 1", chapter)
                .build();

        assertThat(book.getSpine().size()).isEqualTo(1);
        assertThat(book.getTableOfContents().getTocReferences()).hasSize(1);
        assertThat(book.getTableOfContents().getTocReferences().get(0).getTitle()).isEqualTo("Chapter 1");
    }

    @Test
    void builder_withPublisherAndDescription_setsBothInMetadata() {
        Book book = Book.builder()
                .publisher("Acme Press")
                .description("A test EPUB book")
                .build();

        assertThat(book.getMetadata().getPublishers()).containsExactly("Acme Press");
        assertThat(book.getMetadata().getDescriptions()).containsExactly("A test EPUB book");
    }

    @Test
    void builder_emptyBuild_returnsValidBook() {
        Book book = Book.builder().build();

        assertThat(book).isNotNull();
        assertThat(book.getMetadata()).isNotNull();
        assertThat(book.getSpine()).isNotNull();
        assertThat(book.getResources()).isNotNull();
    }

    @Test
    void builder_coverImage_registeredInResources() {
        Resource cover = Resource.fromBytes(new byte[]{1, 2, 3}, "images/cover.jpg");

        Book book = Book.builder()
                .coverImage(cover)
                .build();

        assertThat(book.getCoverImage()).isEqualTo(cover);
        assertThat(book.getResources().containsByHref("images/cover.jpg")).isTrue();
    }
}
