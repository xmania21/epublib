package nl.siegmann.epublib.viewer.model;

/**
 * Available reading themes. Each theme provides its colour tokens and the
 * path to the CSS file that is injected into the WebView.
 */
public enum ReadingTheme {

    LIGHT("Light", "#ffffff", "#1a1a1a", "#2563eb", "styles/light-theme.css"),
    SEPIA("Sepia", "#f5e6c8", "#3b2a1a", "#8b5c2a", "styles/sepia-theme.css"),
    SOLARIZED_LIGHT("Solarized", "#fdf6e3", "#657b83", "#268bd2", "styles/solarized-theme.css"),
    DARK("Dark", "#1a1a2e", "#e8e0d5", "#e8a45a", "styles/dark-theme.css"),
    NIGHT("Night", "#0a0a0a", "#e8e0d5", "#e8a45a", "styles/night-theme.css"),
    OLED_BLACK("OLED", "#000000", "#e0e0e0", "#ff3366", "styles/oled-theme.css");

    private final String displayName;
    private final String background;
    private final String textColor;
    private final String accentColor;
    private final String cssResourcePath;

    ReadingTheme(String displayName, String background, String textColor,
                 String accentColor, String cssResourcePath) {
        this.displayName = displayName;
        this.background = background;
        this.textColor = textColor;
        this.accentColor = accentColor;
        this.cssResourcePath = cssResourcePath;
    }

    public String getDisplayName()   { return displayName; }
    public String getBackground()    { return background; }
    public String getTextColor()     { return textColor; }
    public String getAccentColor()   { return accentColor; }
    public String getCssResourcePath() { return cssResourcePath; }
}
