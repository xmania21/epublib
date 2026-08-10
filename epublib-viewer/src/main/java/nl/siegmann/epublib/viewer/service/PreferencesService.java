package nl.siegmann.epublib.viewer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.siegmann.epublib.viewer.model.ReadingPreferences;
import nl.siegmann.epublib.viewer.model.ReadingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and persists {@link ReadingPreferences} and per-book {@link ReadingState}
 * to {@code ~/.epublib/} as JSON files.
 *
 * <p>All I/O is intentionally synchronous and lightweight — call from a
 * background thread if needed to avoid blocking the FX thread.</p>
 */
public class PreferencesService {
    
    private static final Logger log = LoggerFactory.getLogger(PreferencesService.class);

    private static final String DIR_NAME = ".epublib";
    private static final String PREFS_FILE = "prefs.json";
    private static final String STATE_DIR = "state";

    private final Path baseDir;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Singleton instance. */
    private static final PreferencesService INSTANCE = new PreferencesService();

    public static PreferencesService getInstance() {
        return INSTANCE;
    }

    private PreferencesService() {
        baseDir = Path.of(System.getProperty("user.home"), DIR_NAME);
        try {
            Files.createDirectories(baseDir);
            Files.createDirectories(baseDir.resolve(STATE_DIR));
        } catch (IOException e) {
            log.error("Cannot create config dir: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Preferences

    /** Returns stored preferences, or defaults if the file does not exist. */
    public ReadingPreferences loadPreferences() {
        File file = baseDir.resolve(PREFS_FILE).toFile();
        if (!file.exists()) return new ReadingPreferences();
        try {
            return mapper.readValue(file, ReadingPreferences.class);
        } catch (IOException e) {
            log.error("Could not read prefs: {}", e.getMessage());
            return new ReadingPreferences();
        }
    }

    /** Persists {@code prefs} to disk. Silently logs on failure. */
    public void savePreferences(ReadingPreferences prefs) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(baseDir.resolve(PREFS_FILE).toFile(), prefs);
        } catch (IOException e) {
            log.error("Could not save prefs: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Per-book reading state

    /** Returns the last reading state for {@code bookId}, or a fresh one. */
    public ReadingState loadState(String bookId) {
        File file = stateFile(bookId);
        if (!file.exists()) return new ReadingState(bookId, "", 0.0, 0);
        try {
            return mapper.readValue(file, ReadingState.class);
        } catch (IOException e) {
            return new ReadingState(bookId, "", 0.0, 0);
        }
    }

    /** Persists the reading state for a book. */
    public void saveState(ReadingState state) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(stateFile(state.getBookId()), state);
        } catch (IOException e) {
            log.error("Could not save state: {}", e.getMessage());
        }
    }

    private File stateFile(String bookId) {
        // Sanitise bookId for use as a filename
        String safe = bookId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return baseDir.resolve(STATE_DIR).resolve(safe + ".json").toFile();
    }
}
