package nl.siegmann.epublib.viewer;

import java.awt.Color;

public enum ReaderTheme {
	LIGHT("Light Page", new Color(255, 255, 255), new Color(34, 34, 34), new Color(0, 102, 204), "#FFFFFF", "#222222", "#0066CC"),
	DARK("Dark / Night Page", new Color(30, 30, 30), new Color(224, 224, 224), new Color(100, 181, 246), "#1E1E1E", "#E0E0E0", "#64B5F6"),
	SEPIA("Warm Sepia Page", new Color(251, 240, 217), new Color(95, 75, 50), new Color(140, 80, 32), "#FBF0D9", "#5F4B32", "#8C5020");

	private final String displayName;
	private final Color backgroundColor;
	private final Color textColor;
	private final Color linkColor;
	private final String bgHex;
	private final String textHex;
	private final String linkHex;

	ReaderTheme(String displayName, Color backgroundColor, Color textColor, Color linkColor, String bgHex, String textHex, String linkHex) {
		this.displayName = displayName;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
		this.linkColor = linkColor;
		this.bgHex = bgHex;
		this.textHex = textHex;
		this.linkHex = linkHex;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	public Color getLinkColor() {
		return linkColor;
	}

	public String getBgHex() {
		return bgHex;
	}

	public String getTextHex() {
		return textHex;
	}

	public String getLinkHex() {
		return linkHex;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
