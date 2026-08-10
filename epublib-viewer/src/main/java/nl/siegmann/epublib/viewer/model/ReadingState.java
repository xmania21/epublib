package nl.siegmann.epublib.viewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Snapshot of the current reading position within a specific book.
 * Serialised alongside {@link ReadingPreferences} by
 * {@link nl.siegmann.epublib.viewer.service.PreferencesService}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingState {

    /** Stable identifier derived from the book's unique identifier metadata. */
    private String bookId = "";

    /** OPF href of the currently displayed spine item. */
    private String chapterHref = "";

    /** Vertical scroll position inside the WebView (pixels). */
    private double scrollY = 0.0;

    /** Index of the spine item (0-based). */
    private int spineIndex = 0;

    private java.util.List<Bookmark> bookmarks = new java.util.ArrayList<>();

    public ReadingState() {}

    public ReadingState(String bookId, String chapterHref, double scrollY, int spineIndex) {
        this.bookId = bookId;
        this.chapterHref = chapterHref;
        this.scrollY = scrollY;
        this.spineIndex = spineIndex;
    }

    public String getBookId()                     { return bookId; }
    public void setBookId(String bookId)          { this.bookId = bookId; }

    public String getChapterHref()                { return chapterHref; }
    public void setChapterHref(String href)       { this.chapterHref = href; }

    public double getScrollY()                    { return scrollY; }
    public void setScrollY(double scrollY)        { this.scrollY = scrollY; }

    public int getSpineIndex()                    { return spineIndex; }
    public void setSpineIndex(int spineIndex)     { this.spineIndex = spineIndex; }

    public java.util.List<Bookmark> getBookmarks() { return bookmarks; }
    public void setBookmarks(java.util.List<Bookmark> bookmarks) { this.bookmarks = bookmarks; }
}
