package nl.siegmann.epublib.viewer;

import java.awt.Color;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ViewerTheme {
	LIGHT("Light Mode", true, new Color(255, 255, 255), new Color(34, 34, 34), new Color(0, 102, 204), "#FFFFFF", "#222222", "#0066CC"),
	DARK("Dark Mode", false, new Color(30, 30, 30), new Color(224, 224, 224), new Color(100, 181, 246), "#1E1E1E", "#E0E0E0", "#64B5F6"),
	SEPIA("Warm Sepia", true, new Color(251, 240, 217), new Color(95, 75, 50), new Color(140, 80, 32), "#FBF0D9", "#5F4B32", "#8C5020");

	private static final Logger log = LoggerFactory.getLogger(ViewerTheme.class);

	private final String displayName;
	private final boolean isLight;
	private final Color backgroundColor;
	private final Color textColor;
	private final Color linkColor;
	private final String bgHex;
	private final String textHex;
	private final String linkHex;

	ViewerTheme(String displayName, boolean isLight, Color backgroundColor, Color textColor, Color linkColor, String bgHex, String textHex, String linkHex) {
		this.displayName = displayName;
		this.isLight = isLight;
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

	public boolean isLight() {
		return isLight;
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

	public void setupLaf() {
		try {
			if (isLight) {
				FlatLightLaf.setup();
			} else {
				FlatDarkLaf.setup();
			}
			FlatLaf.updateUI();
		} catch (Exception e) {
			log.error("Unable to set theme look & feel", e);
		}
	}

	@Override
	public String toString() {
		return displayName;
	}
}
