package nl.siegmann.epublib.viewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Persisted reading preferences. Serialised to/from JSON by
 * {@link nl.siegmann.epublib.viewer.service.PreferencesService}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingPreferences {

    // --- Appearance ---
    private ReadingTheme theme = ReadingTheme.NIGHT;
    private String fontFamily = "Georgia, serif";
    private int fontSize = 18;
    private double lineHeight = 1.0;
    private int marginTop = 20;
    private int marginBottom = 20;
    private int marginLeft = 40;
    private int marginRight = 40;
    private int paragraphSpacing = 0;
    private double letterSpacing = 0.0;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private String textAlign = "left";
    // --- Navigation ---
    private TapZonePreference tapZone = TapZonePreference.CLASSIC_LCR;



    public ReadingTheme getTheme()                  { return theme; }
    public void setTheme(ReadingTheme theme)        { this.theme = theme; }

    public String getFontFamily()                   { return fontFamily; }
    public void setFontFamily(String fontFamily)    { this.fontFamily = fontFamily; }

    public int getFontSize()                        { return fontSize; }
    public void setFontSize(int fontSize)           { this.fontSize = fontSize; }

    public double getLineHeight()                   { return lineHeight; }
    public void setLineHeight(double lineHeight)    { this.lineHeight = lineHeight; }

    public int getMarginTop() { return marginTop; }
    public void setMarginTop(int m) { this.marginTop = m; }

    public int getMarginBottom() { return marginBottom; }
    public void setMarginBottom(int m) { this.marginBottom = m; }

    public int getMarginLeft() { return marginLeft; }
    public void setMarginLeft(int m) { this.marginLeft = m; }

    public int getMarginRight() { return marginRight; }
    public void setMarginRight(int m) { this.marginRight = m; }

    public int getParagraphSpacing() { return paragraphSpacing; }
    public void setParagraphSpacing(int p) { this.paragraphSpacing = p; }

    public double getLetterSpacing() { return letterSpacing; }
    public void setLetterSpacing(double l) { this.letterSpacing = l; }

    public TapZonePreference getTapZone()                  { return tapZone; }
    public void setTapZone(TapZonePreference tapZone)      { this.tapZone = tapZone; }



    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }

    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }

    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }

    public String getTextAlign() { return textAlign; }
    public void setTextAlign(String textAlign) { this.textAlign = textAlign; }
}
