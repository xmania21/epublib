package nl.siegmann.epublib.domain;

/**
 * A Java 17 Record representing a custom EPUB 3 manifest property.
 */
public record ManifestPropertyRecord(String name, String value) implements ManifestProperties {

	public ManifestPropertyRecord {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Manifest property name cannot be blank");
		}
	}

	@Override
	public String getName() {
		return name;
	}
}
