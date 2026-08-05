package nl.siegmann.epublib.domain;

/**
 * EPUB 3 Rendition Layout properties (Fixed-Layout vs Reflowable).
 */
public enum RenditionLayout {
	REFLOWABLE("reflowable"),
	PRE_PAGINATED("pre-paginated");

	private final String value;

	RenditionLayout(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static RenditionLayout fromValue(String value) {
		if (value == null) return null;
		for (RenditionLayout layout : values()) {
			if (layout.value.equalsIgnoreCase(value.trim())) {
				return layout;
			}
		}
		return null;
	}
}
