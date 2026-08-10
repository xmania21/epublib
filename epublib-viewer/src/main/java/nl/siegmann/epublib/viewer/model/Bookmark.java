package nl.siegmann.epublib.viewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bookmark {
    private String title;
    private int spineIndex;

    public Bookmark() {}

    public Bookmark(String title, int spineIndex) {
        this.title = title;
        this.spineIndex = spineIndex;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getSpineIndex() { return spineIndex; }
    public void setSpineIndex(int spineIndex) { this.spineIndex = spineIndex; }

    @Override
    public String toString() {
        return title;
    }
}
