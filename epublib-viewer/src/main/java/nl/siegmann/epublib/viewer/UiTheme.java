package nl.siegmann.epublib.viewer;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum UiTheme {
	LIGHT("Light UI", true),
	DARK("Dark UI", false);

	private static final Logger log = LoggerFactory.getLogger(UiTheme.class);

	private final String displayName;
	private final boolean isLight;

	UiTheme(String displayName, boolean isLight) {
		this.displayName = displayName;
		this.isLight = isLight;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isLight() {
		return isLight;
	}

	public void apply() {
		try {
			if (isLight) {
				FlatLightLaf.setup();
			} else {
				FlatDarkLaf.setup();
			}
			FlatLaf.updateUI();
		} catch (Exception e) {
			log.error("Unable to apply UI Theme: {}", displayName, e);
		}
	}

	@Override
	public String toString() {
		return displayName;
	}
}
