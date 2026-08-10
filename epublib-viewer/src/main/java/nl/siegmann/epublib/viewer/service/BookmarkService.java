package nl.siegmann.epublib.viewer.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages per-book bookmarks, persisting them to
 * {@code ~/.epublib/bookmarks/<bookId>.json}.
 */
public class BookmarkService {
    
    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);

    private static final String DIR_NAME = ".epublib";
    private static final String BOOKMARK_DIR = "bookmarks";

    private final Path bookmarkDir;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final BookmarkService INSTANCE = new BookmarkService();

    public static BookmarkService getInstance() { return INSTANCE; }

    private BookmarkService() {
        bookmarkDir = Path.of(System.getProperty("user.home"), DIR_NAME, BOOKMARK_DIR);
        try {
            Files.createDirectories(bookmarkDir);
        } catch (IOException e) {
            log.error("Cannot create bookmark dir: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------

    /** A single bookmark entry. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bookmark {
        public String chapterHref = "";
        public double scrollY = 0.0;
        public String label = "";
        public long createdAt = System.currentTimeMillis();

        public Bookmark() {}

        public Bookmark(String chapterHref, double scrollY, String label) {
            this.chapterHref = chapterHref;
            this.scrollY = scrollY;
            this.label = label;
        }
    }

    // -------------------------------------------------------------------------

    /** Loads all bookmarks for a given book. Returns an empty list on failure. */
    @SuppressWarnings("unchecked")
    public List<Bookmark> loadBookmarks(String bookId) {
        File file = bookmarkFile(bookId);
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file,
                mapper.getTypeFactory().constructCollectionType(List.class, Bookmark.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** Adds a bookmark and persists the updated list. */
    public void addBookmark(String bookId, Bookmark bookmark) {
        List<Bookmark> list = loadBookmarks(bookId);
        list.add(bookmark);
        save(bookId, list);
    }

    /** Removes the bookmark at the given index and persists. */
    public void removeBookmark(String bookId, int index) {
        List<Bookmark> list = loadBookmarks(bookId);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            save(bookId, list);
        }
    }

    private void save(String bookId, List<Bookmark> list) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(bookmarkFile(bookId), list);
        } catch (IOException e) {
            log.error("Save failed: {}", e.getMessage());
        }
    }

    private File bookmarkFile(String bookId) {
        String safe = bookId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return bookmarkDir.resolve(safe + ".json").toFile();
    }
}
