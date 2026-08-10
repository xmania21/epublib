package nl.siegmann.epublib.viewer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.viewer.model.LibraryBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LibraryService {
    
    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);

    private static final String DIR_NAME = ".epublib";
    private static final String LIBRARY_DIR = "library";
    private static final String EPUBS_DIR = "epubs";
    private static final String COVERS_DIR = "covers";
    private static final String LIBRARY_FILE = "library.json";

    private final Path baseDir;
    private final Path epubsDir;
    private final Path coversDir;
    private final ObjectMapper mapper = new ObjectMapper();
    private List<LibraryBook> cachedLibrary;

    private static final LibraryService INSTANCE = new LibraryService();

    public static LibraryService getInstance() {
        return INSTANCE;
    }

    private LibraryService() {
        baseDir = Path.of(System.getProperty("user.home"), DIR_NAME, LIBRARY_DIR);
        epubsDir = baseDir.resolve(EPUBS_DIR);
        coversDir = baseDir.resolve(COVERS_DIR);
        try {
            Files.createDirectories(epubsDir);
            Files.createDirectories(coversDir);
        } catch (IOException e) {
            log.error("Cannot create library dirs: {}", e.getMessage());
        }
    }

    public List<LibraryBook> loadLibrary() {
        if (cachedLibrary != null) return cachedLibrary;

        File file = baseDir.resolve(LIBRARY_FILE).toFile();
        if (!file.exists()) {
            cachedLibrary = new ArrayList<>();
            return cachedLibrary;
        }

        try {
            cachedLibrary = mapper.readValue(file, new TypeReference<List<LibraryBook>>(){});
        } catch (IOException e) {
            log.error("Could not read library: {}", e.getMessage());
            cachedLibrary = new ArrayList<>();
        }
        return cachedLibrary;
    }

    public void saveLibrary() {
        if (cachedLibrary == null) return;
        try {
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(baseDir.resolve(LIBRARY_FILE).toFile(), cachedLibrary);
        } catch (IOException e) {
            log.error("Could not save library: {}", e.getMessage());
        }
    }

    public LibraryBook importEpub(File sourceFile) throws Exception {
        // Read metadata
        Book epubBook;
        try (InputStream is = new FileInputStream(sourceFile)) {
            epubBook = new EpubReader().readEpub(is);
        }

        String id = UUID.randomUUID().toString();
        String title = epubBook.getTitle();
        if (title == null || title.isBlank()) {
            title = sourceFile.getName();
        }

        String author = "Unknown Author";
        if (!epubBook.getMetadata().getAuthors().isEmpty()) {
            var a = epubBook.getMetadata().getAuthors().get(0);
            author = (a.getFirstname() == null ? "" : a.getFirstname() + " ") + 
                     (a.getLastname() == null ? "" : a.getLastname());
            author = author.trim();
        }

        // Copy EPUB
        String ext = sourceFile.getName().substring(sourceFile.getName().lastIndexOf('.'));
        Path destEpub = epubsDir.resolve(id + ext);
        Files.copy(sourceFile.toPath(), destEpub, StandardCopyOption.REPLACE_EXISTING);

        // Extract Cover
        String coverPath = null;
        Resource coverImage = epubBook.getCoverImage();
        if (coverImage != null) {
            String coverExt = ".jpg";
            if (coverImage.getMediaType().getName().contains("png")) coverExt = ".png";
            Path destCover = coversDir.resolve(id + coverExt);
            Files.write(destCover, coverImage.getData());
            coverPath = destCover.toAbsolutePath().toString();
        }

        LibraryBook lb = new LibraryBook(id, title, author, destEpub.toAbsolutePath().toString(), coverPath);
        
        loadLibrary().add(lb);
        saveLibrary();
        
        return lb;
    }

    public List<LibraryBook> importFolder(File folder) {
        List<LibraryBook> imported = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".epub"));
        if (files != null) {
            for (File file : files) {
                try {
                    imported.add(importEpub(file));
                } catch (Exception e) {
                    log.error("Failed to import {}: {}", file.getName(), e.getMessage());
                }
            }
        }
        return imported;
    }
}
