package nl.siegmann.epublib.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Metadata#validate()}.
 *
 * @since 5.0
 */
class MetadataValidationTest {

    @Test
    void validate_fullyPopulatedMetadata_returnsNoIssues() {
        Metadata metadata = new Metadata();
        metadata.addTitle("Valid Book Title");
        metadata.addAuthor(Author.of("Jane", "Doe"));
        metadata.addIdentifier(Identifier.of(Identifier.Scheme.ISBN, "978-3-16-148410-0", true));
        metadata.setLanguage("en");

        assertThat(metadata.validate()).isEmpty();
    }

    @Test
    void validate_missingTitle_returnsError() {
        Metadata metadata = new Metadata();
        // no title added, language and identifier present
        metadata.setLanguage("en");

        java.util.List<ValidationIssue> issues = metadata.validate();

        assertThat(issues).anyMatch(i ->
                i.getSeverity() == ValidationIssue.Severity.ERROR
                        && i.getField().equals("title"));
    }

    @Test
    void validate_blankTitleOnly_returnsError() {
        Metadata metadata = new Metadata();
        metadata.addTitle("   "); // blank
        metadata.setLanguage("en");

        java.util.List<ValidationIssue> issues = metadata.validate();

        assertThat(issues).anyMatch(i ->
                i.getSeverity() == ValidationIssue.Severity.ERROR
                        && i.getField().equals("title"));
    }

    @Test
    void validate_missingLanguage_returnsError() {
        Metadata metadata = new Metadata();
        metadata.addTitle("My Book");
        metadata.setLanguage(null);

        java.util.List<ValidationIssue> issues = metadata.validate();

        assertThat(issues).anyMatch(i ->
                i.getSeverity() == ValidationIssue.Severity.ERROR
                        && i.getField().equals("language"));
    }

    @Test
    void validate_noAuthor_returnsWarning() {
        Metadata metadata = new Metadata();
        metadata.addTitle("My Book");
        metadata.setLanguage("en");

        java.util.List<ValidationIssue> issues = metadata.validate();

        assertThat(issues).anyMatch(i ->
                i.getSeverity() == ValidationIssue.Severity.WARNING
                        && i.getField().equals("author"));
    }

    @Test
    void validate_missingIdentifierValue_returnsError() {
        Metadata metadata = new Metadata();
        metadata.addTitle("My Book");
        metadata.setLanguage("en");
        metadata.addAuthor(Author.of("Jane", "Doe"));
        // Override the auto-generated UUID with a blank-value identifier
        metadata.setIdentifiers(java.util.Collections.singletonList(new Identifier("UUID", "")));

        java.util.List<ValidationIssue> issues = metadata.validate();

        assertThat(issues).anyMatch(i ->
                i.getSeverity() == ValidationIssue.Severity.ERROR
                        && i.getField().equals("identifier"));
    }

    @Test
    void validate_validationIssue_toStringIncludesSeverityAndField() {
        ValidationIssue issue = new ValidationIssue(ValidationIssue.Severity.ERROR, "title", "Missing title");
        assertThat(issue.toString()).contains("ERROR").contains("title").contains("Missing title");
    }
}
