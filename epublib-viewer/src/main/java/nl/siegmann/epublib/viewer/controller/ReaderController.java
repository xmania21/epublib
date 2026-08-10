package nl.siegmann.epublib.viewer.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.viewer.model.ReadingPreferences;
import nl.siegmann.epublib.viewer.model.ReadingState;
import nl.siegmann.epublib.viewer.model.TapZonePreference;
import nl.siegmann.epublib.viewer.service.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller for {@code /fxml/ReaderLayout.fxml}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Loading and displaying EPUB spine items in a {@link WebView}</li>
 *   <li>Injecting per-theme CSS into the WebView content</li>
 *   <li>Dispatching canvas taps to the active {@link TapZonePreference}</li>
 *   <li>Fading the header bar in/out on centre-tap</li>
 * </ul>
 */
public class ReaderController {

    private static final Logger log = LoggerFactory.getLogger(ReaderController.class);

    // ---- FXML-injected nodes ----
    @FXML private StackPane rootPane;
    @FXML private WebView webView;
    @FXML private AnchorPane controlPanelAnchor;
    @FXML private AnchorPane tocAnchor;
    @FXML private AnchorPane searchAnchor;

    // ---- Child controllers (loaded automatically via nested fx:include) ----
    @FXML private ControlPanelController controlPanelController;
    @FXML private TocController tocController;
    @FXML private SearchModalController searchModalController;
    @FXML private LibrarySettingsModalController settingsModalController;

    // ---- State ----
    private Book book;
    private int currentSpineIndex = 0;
    private ReadingPreferences prefs = new ReadingPreferences();
    private String currentBookId = "";
    private boolean controlPanelVisible = false;
    private boolean tocVisible = false;
    private nl.siegmann.epublib.search.SearchIndex searchIndex;

    public ControlPanelController getControlPanelController() {
        return controlPanelController;
    }

    // ---- Animation ----
    private static final Duration ANIM_DURATION = Duration.millis(220);

    @FXML
    public void initialize() {
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Wire child controllers if injected by FXMLLoader
        if (controlPanelController != null) {
            controlPanelController.setReaderController(this);
        }
        if (tocController != null) {
            tocController.setReaderController(this);
        }
        if (searchModalController != null) {
            searchModalController.setReaderController(this);
        }
        if (settingsModalController != null) {
            settingsModalController.setReaderController(this);
        }

        engine.setOnStatusChanged(event -> {
            String msg = event.getData();
            if ("CMD_NEXT_CHAPTER".equals(msg)) {
                javafx.application.Platform.runLater(() -> nextPage());
                engine.executeScript("window.status = '';");
            } else if ("CMD_PREV_CHAPTER".equals(msg)) {
                javafx.application.Platform.runLater(() -> prevPage());
                engine.executeScript("window.status = '';");
            }
        });

        webView.setContextMenuEnabled(false);
        
        webView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT:
                case SPACE:
                case PAGE_DOWN:
                    safeExecuteScript("pageDown()");
                    event.consume();
                    break;
                case LEFT:
                case PAGE_UP:
                    safeExecuteScript("pageUp()");
                    event.consume();
                    break;
            }
        });

        webView.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                toggleControlPanel();
                return;
            }
            
            // Ignore clicks on the scrollbars (assume ~20px on the right and bottom edges)
            if (event.getX() > webView.getWidth() - 20 || event.getY() > webView.getHeight() - 20) {
                return;
            }
            
            if (nl.siegmann.epublib.viewer.util.PlatformUtil.isMobile()) {
                TapZonePreference.TapAction action = prefs.getTapZone().resolve(
                    event.getX(), event.getY(),
                    webView.getWidth(), webView.getHeight()
                );
                switch (action) {
                    case PREV     -> safeExecuteScript("pageUp()");
                    case NEXT     -> safeExecuteScript("pageDown()");
                    case SETTINGS -> toggleControlPanel();
                }
            }
        });

        // Swipe gesture support (mobile via GluonFX)
        webView.setOnSwipeLeft(e  -> safeExecuteScript("pageDown()"));
        webView.setOnSwipeRight(e -> safeExecuteScript("pageUp()"));
        webView.setOnSwipeDown(e  -> dismissOverlays());
    }

    // =========================================================================
    // EPUB loading
    // =========================================================================

    /** Opens an EPUB file and loads it into the viewer. */
    public void openEpub(File epubFile) {
        try (InputStream is = new FileInputStream(epubFile)) {
            book = new EpubReader().readEpub(is);
            currentBookId = deriveBookId(epubFile);

            // Restore last reading position
            ReadingState state = PreferencesService.getInstance().loadState(currentBookId);
            currentSpineIndex = Math.max(0, state.getSpineIndex());

            // Populate TOC and Bookmarks
            if (tocController != null) {
                tocController.populateToc(book);
                tocController.refreshBookmarks(state.getBookmarks());
            }

            // Build search index in background
            if (searchModalController != null) {
                searchIndex = new nl.siegmann.epublib.search.SearchIndex();
                searchModalController.setSearchIndex(searchIndex);
                Thread searchThread = new Thread(() -> {
                    searchIndex.initBook(book);
                });
                searchThread.setContextClassLoader(getClass().getClassLoader());
                searchThread.start();
            }

            // Load the first spine item
            loadCurrentSpineItem();
            updateControlPanel();
        } catch (Exception e) {
            log.error("Could not open EPUB: {}", e.getMessage(), e);
            showError("Could not open EPUB: " + e.getMessage());
        }
    }

    private void loadCurrentSpineItem() {
        if (book == null) return;
        List<SpineReference> refs = book.getSpine().getSpineReferences();
        if (refs.isEmpty()) return;
        currentSpineIndex = Math.max(0, Math.min(currentSpineIndex, refs.size() - 1));
        Resource res = refs.get(currentSpineIndex).getResource();
        loadResource(res);
    }

    private void loadResource(Resource res) {
        if (res == null) return;
        Platform.runLater(() -> {
            try {
                String html = new String(res.getData(), StandardCharsets.UTF_8);
                
                // Prepare initial scroll script
                double scrollY = 0;
                if (currentBookId != null && !currentBookId.isEmpty()) {
                    nl.siegmann.epublib.viewer.model.ReadingState state = nl.siegmann.epublib.viewer.service.PreferencesService.getInstance().loadState(currentBookId);
                    if (state != null && state.getSpineIndex() == currentSpineIndex) {
                        scrollY = state.getScrollY();
                    }
                }
                
                String initScript = "<script>\n" +
                    "let isScrolling;\n" +
                    "window.addEventListener('scroll', function (e) {\n" +
                    "  document.body.classList.add('is-scrolling');\n" +
                    "  window.clearTimeout(isScrolling);\n" +
                    "  isScrolling = setTimeout(function() {\n" +
                    "      document.body.classList.remove('is-scrolling');\n" +
                    "  }, 1000);\n" +
                    "}, false);\n" +
                    "window.addEventListener('wheel', function(e) {\n" +
                    "  if (e.deltaY > 0 && window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 2) {\n" +
                    "      window.status = 'CMD_NEXT_CHAPTER';\n" +
                    "  } else if (e.deltaY < 0 && window.scrollY <= 0) {\n" +
                    "      window.status = 'CMD_PREV_CHAPTER';\n" +
                    "  }\n" +
                    "});\n" +
                    "window.onload = function() {\n" +
                    "  if (" + scrollY + " > 0) window.scrollTo(0, " + scrollY + ");\n" +
                    "};\n" +
                    "function pageDown() {\n" +
                    "  if (window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 2) {\n" +
                    "      window.status = 'CMD_NEXT_CHAPTER';\n" +
                    "  } else {\n" +
                    "      window.scrollBy(0, window.innerHeight * 0.9);\n" +
                    "  }\n" +
                    "}\n" +
                    "function pageUp() {\n" +
                    "  if (window.scrollY <= 0) {\n" +
                    "      window.status = 'CMD_PREV_CHAPTER';\n" +
                    "  } else {\n" +
                    "      window.scrollBy(0, -window.innerHeight * 0.9);\n" +
                    "  }\n" +
                    "}\n" +
                    "</script>\n";

                if (html.toLowerCase().contains("</head>")) {
                    html = html.replaceFirst("(?i)</head>", initScript + "</head>");
                } else if (html.toLowerCase().contains("<body")) {
                    html = html.replaceFirst("(?i)(<body[^>]*>)", initScript + "$1");
                } else {
                    html = initScript + html;
                }

                webView.getEngine().loadContent(html, "text/html");
            } catch (Exception e) {
                log.error("Could not load chapter: {}", e.getMessage(), e);
                showError("Could not load chapter: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // CSS theme injection
    // =========================================================================

    private String buildDynamicCss() {
        nl.siegmann.epublib.viewer.model.ReadingTheme theme = prefs.getTheme();
        StringBuilder css = new StringBuilder();

        css.append(String.format("body, body * { background: %s !important; color: %s !important; }\n", 
                                 theme.getBackground(), theme.getTextColor()));
        css.append(String.format("a, a * { color: %s !important; }\n", theme.getAccentColor()));
        css.append("::-webkit-scrollbar-thumb { background: transparent; border-radius: 4px; }\n");
        css.append(String.format("body.is-scrolling ::-webkit-scrollbar-thumb { background: %s; }\n", theme.getAccentColor()));
        css.append("::-webkit-scrollbar { width: 8px; height: 8px; background: transparent; }\n");
        css.append(String.format("::-webkit-scrollbar-track { background: %s; }\n", theme.getBackground()));
        css.append(String.format("::-webkit-scrollbar-corner { background: %s; }\n", theme.getBackground()));

        String safeFont = prefs.getFontFamily().replace("\\", "\\\\").replace("'", "\\'");
        
        css.append(String.format(java.util.Locale.US,
            "body {\n" +
            "  font-family: '%s' !important;\n" +
            "  font-size: %dpx !important;\n" +
            "  line-height: %.2f !important;\n" +
            "  padding: %dpx %dpx %dpx %dpx !important;\n" +
            "  letter-spacing: %.1fpx !important;\n" +
            "  font-weight: %s !important;\n" +
            "  font-style: %s !important;\n" +
            "  text-decoration: %s !important;\n" +
            "  text-align: %s !important;\n" +
            "}\n",
            safeFont, prefs.getFontSize(), prefs.getLineHeight(),
            prefs.getMarginTop(), prefs.getMarginRight(), prefs.getMarginBottom(), prefs.getMarginLeft(),
            prefs.getLetterSpacing(),
            prefs.isBold() ? "bold" : "normal",
            prefs.isItalic() ? "italic" : "normal",
            prefs.isUnderline() ? "underline" : "none",
            prefs.getTextAlign() != null ? prefs.getTextAlign() : "left"
        ));

        css.append(String.format(java.util.Locale.US,
            "body * {\n" +
            "  font-size: %dpx !important;\n" +
            "  line-height: %.2f !important;\n" +
            "  font-weight: %s !important;\n" +
            "  font-style: %s !important;\n" +
            "  text-decoration: %s !important;\n" +
            "  text-align: %s !important;\n" +
            "}\n",
            prefs.getFontSize(), prefs.getLineHeight(),
            prefs.isBold() ? "bold" : "normal",
            prefs.isItalic() ? "italic" : "normal",
            prefs.isUnderline() ? "underline" : "none",
            prefs.getTextAlign() != null ? prefs.getTextAlign() : "left"
        ));

        css.append(String.format("p, div { margin-bottom: %dpx !important; }\n", prefs.getParagraphSpacing()));
        css.append("img { max-width: 100%; height: auto; }\n");

        return css.toString();
    }

    /** Re-renders the current page with the updated theme/font settings. */
    private void refreshContent() {
        loadCurrentSpineItem();
    }


    private Object safeExecuteScript(String script) {
        if (webView.getEngine().getLoadWorker().getState() != javafx.concurrent.Worker.State.SUCCEEDED) {
            return null;
        }
        try {
            return webView.getEngine().executeScript(script);
        } catch (Exception e) {
            return null;
        }
    }

    public void refreshReader() {
        Platform.runLater(() -> {
            try {
                // Apply theme to JavaFX UI
                rootPane.getStyleClass().removeAll(
                    "theme-light", "theme-sepia", "theme-dark", "theme-night",
                    "theme-solarized_light", "theme-oled_black"
                );
                rootPane.getStyleClass().add("theme-" + prefs.getTheme().name().toLowerCase());

                // Inject CSS via JavaFX WebEngine user style sheet location (bypasses executeScript)
                String css = buildDynamicCss();
                String dataUri = "data:text/css;charset=utf-8," + java.net.URLEncoder.encode(css, "UTF-8").replace("+", "%20");
                webView.getEngine().setUserStyleSheetLocation(dataUri);
            } catch (Exception e) {
                log.error("refreshReader failed: {}", e.getMessage());
            }
        });
    }

    private void restoreScrollPosition() {
        if (currentBookId != null && !currentBookId.isEmpty()) {
            Platform.runLater(() -> {
                try {
                    nl.siegmann.epublib.viewer.model.ReadingState state = nl.siegmann.epublib.viewer.service.PreferencesService.getInstance().loadState(currentBookId);
                    if (state != null && state.getScrollY() > 0) {
                        safeExecuteScript("window.scrollTo(0, " + state.getScrollY() + ");");
                    }
                } catch (Exception ignored) {}
            });
        }
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    public void nextPage() {
        if (book == null) return;
        int total = book.getSpine().getSpineReferences().size();
        if (currentSpineIndex < total - 1) {
            currentSpineIndex++;
            loadCurrentSpineItem();
            updateControlPanel();
        }
    }

    public void prevPage() {
        if (book == null || currentSpineIndex <= 0) return;
        currentSpineIndex--;
        loadCurrentSpineItem();
        updateControlPanel();
    }

    public void goToSpineIndex(int index) {
        currentSpineIndex = index;
        loadCurrentSpineItem();
        updateControlPanel();
    }

    // =========================================================================
    // Control panel
    // =========================================================================

    public void toggleControlPanel() {
        controlPanelVisible = !controlPanelVisible;
        if (controlPanelVisible && tocVisible) {
            // Dismiss TOC drawer so it doesn't overlap
            toggleToc();
        }
        if (controlPanelController != null) {
            controlPanelController.setVisible(controlPanelVisible);
        }
    }

    public void dismissOverlays() {
        if (controlPanelVisible) toggleControlPanel();
        if (tocVisible) toggleToc();
    }

    private void updateControlPanel() {
        if (controlPanelController != null && book != null) {
            int total = book.getSpine().getSpineReferences().size();
            controlPanelController.updateProgress(currentSpineIndex, total,
                getCurrentChapterTitle());
        }
    }

    private String getCurrentChapterTitle() {
        if (book == null) return "";
        List<SpineReference> refs = book.getSpine().getSpineReferences();
        if (currentSpineIndex >= refs.size()) return "";
        Resource res = refs.get(currentSpineIndex).getResource();
        String title = res.getTitle() != null ? res.getTitle() : "Chapter " + (currentSpineIndex + 1);
        return title;
    }

    public void openSettingsModal() {
        if (settingsModalController != null) {
            settingsModalController.showModal();
        }
    }

    // =========================================================================
    // TOC
    // =========================================================================

    public void toggleToc() {
        tocVisible = !tocVisible;
        if (tocVisible && controlPanelVisible) {
            // Dismiss Control Panel so it doesn't overlap with TOC drawer
            toggleControlPanel();
        }
        if (tocController != null) {
            if (tocVisible) {
                tocController.openTocTab();
                tocController.setActiveIndex(currentSpineIndex);
            }
            tocController.setVisible(tocVisible);
        }
    }

    public boolean isCurrentPageBookmarked() {
        if (currentBookId == null || currentBookId.isEmpty()) return false;
        nl.siegmann.epublib.viewer.model.ReadingState state = nl.siegmann.epublib.viewer.service.PreferencesService.getInstance().loadState(currentBookId);
        for (nl.siegmann.epublib.viewer.model.Bookmark b : state.getBookmarks()) {
            if (b.getSpineIndex() == currentSpineIndex) return true;
        }
        return false;
    }

    public boolean toggleBookmark() {
        if (currentBookId == null || currentBookId.isEmpty()) return false;
        nl.siegmann.epublib.viewer.model.ReadingState state = nl.siegmann.epublib.viewer.service.PreferencesService.getInstance().loadState(currentBookId);
        
        boolean found = false;
        java.util.Iterator<nl.siegmann.epublib.viewer.model.Bookmark> it = state.getBookmarks().iterator();
        while (it.hasNext()) {
            if (it.next().getSpineIndex() == currentSpineIndex) {
                it.remove();
                found = true;
                break;
            }
        }
        
        if (!found) {
            String title = getCurrentChapterTitle();
            state.getBookmarks().add(new nl.siegmann.epublib.viewer.model.Bookmark(title, currentSpineIndex));
        }
        
        nl.siegmann.epublib.viewer.service.PreferencesService.getInstance().saveState(state);
        
        if (tocController != null) {
            tocController.refreshBookmarks(state.getBookmarks());
        }
        return !found;
    }

    // =========================================================================
    // Search & Navigation
    // =========================================================================

    public void openSearchModal() {
        if (controlPanelVisible) {
            toggleControlPanel();
        }
        if (searchModalController != null) {
            searchModalController.showModal();
        }
    }

    public void jumpToSearchResult(nl.siegmann.epublib.search.SearchResult result) {
        if (result == null || result.getResource() == null) return;
        
        // Find spine index for resource
        int index = -1;
        for (int i = 0; i < book.getSpine().size(); i++) {
            if (book.getSpine().getSpineReferences().get(i).getResource().getHref().equals(result.getResource().getHref())) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            goToSpineIndex(index);
            // Highlight exact occurrence
            final String term = result.getSearchTerm().replace("'", "\\'");
            final int matchIndex = result.getMatchIndex();
            
            // Wait for WebWorker to finish load before searching
            webView.getEngine().getLoadWorker().stateProperty().addListener(new javafx.beans.value.ChangeListener<javafx.concurrent.Worker.State>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends javafx.concurrent.Worker.State> obs, javafx.concurrent.Worker.State oldState, javafx.concurrent.Worker.State newState) {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        webView.getEngine().getLoadWorker().stateProperty().removeListener(this);
                        
                        String script = 
                            "if (window.find) {" +
                            "  var sel = window.getSelection();" +
                            "  if (sel) sel.removeAllRanges();" +
                            "  for (var i = 0; i <= " + matchIndex + "; i++) {" +
                            "    window.find('" + term + "', false, false, true, false, false, false);" +
                            "  }" +
                            "}";
                        safeExecuteScript(script);
                    }
                }
            });
        }
    }

    public void executeSearch(String term, boolean backwards) {
        Platform.runLater(() -> {
            if (term == null || term.isEmpty()) {
                clearSearchSelection();
                return;
            }
            try {
                String script = String.format("if (window.find) { window.find('%s', false, %b, true, false, false, false); }",
                    term.replace("'", "\\'"), backwards);
                safeExecuteScript(script);
            } catch (Exception ignored) {}
        });
    }

    public void clearSearchSelection() {
        Platform.runLater(() -> {
            try {
                safeExecuteScript("if (window.getSelection) { window.getSelection().removeAllRanges(); }");
            } catch (Exception ignored) {}
        });
    }

    // =========================================================================
    // Theme & font
    // =========================================================================

    public void toggleTheme() {
        nl.siegmann.epublib.viewer.model.ReadingTheme[] themes =
            nl.siegmann.epublib.viewer.model.ReadingTheme.values();
        int next = (prefs.getTheme().ordinal() + 1) % themes.length;
        prefs.setTheme(themes[next]);
        refreshContent();
    }

    public void fontSizeUp() {
        prefs.setFontSize(prefs.getFontSize() + 1);
        refreshContent();
    }

    public void fontSizeDown() {
        prefs.setFontSize(prefs.getFontSize() - 1);
        refreshContent();
    }

    // =========================================================================
    // Preferences
    // =========================================================================

    /** Called by {@link nl.siegmann.epublib.viewer.EpubViewerApp} on startup. */
    public void applyPreferences(ReadingPreferences newPrefs) {
        this.prefs = newPrefs;
        // CRITICAL: propagate the same prefs instance to ControlPanel so both
        // controllers always read/write the same object.
        if (controlPanelController != null) {
            controlPanelController.applyPreferences(newPrefs);
        }
    }

    /** Returns the current live preferences object (may have been mutated). */
    public ReadingPreferences getCurrentPreferences() {
        return prefs;
    }

    /** Saves the current reading position. */
    public void persistState() {
        if (book == null) return;
        double scrollY = 0;
        try {
            Object result = webView.getEngine().executeScript("window.scrollY");
            if (result instanceof Number n) scrollY = n.doubleValue();
        } catch (Exception ignored) {}
        PreferencesService.getInstance().saveState(
            new ReadingState(currentBookId, getCurrentChapterHref(), scrollY, currentSpineIndex)
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String getCurrentChapterHref() {
        if (book == null) return "";
        List<SpineReference> refs = book.getSpine().getSpineReferences();
        return currentSpineIndex < refs.size()
            ? refs.get(currentSpineIndex).getResource().getHref()
            : "";
    }

    private String deriveBookId(File file) {
        if (book != null) {
            List<String> ids = book.getMetadata().getIdentifiers()
                .stream().map(Object::toString).toList();
            if (!ids.isEmpty()) return ids.get(0);
        }
        return file.getName();
    }

    private void fadeNode(javafx.scene.Node node, double toValue) {
        FadeTransition ft = new FadeTransition(ANIM_DURATION, node);
        ft.setToValue(toValue);
        ft.play();
        node.setVisible(toValue > 0 || node.getOpacity() > 0);
    }

    private void showError(String msg) {
        webView.getEngine().loadContent(
            "<body style='background:#1a1a2e;color:#e8e0d5;padding:2em;font-family:sans-serif'>"
            + "<h2>⚠ Error</h2><p>" + msg + "</p></body>", "text/html"
        );
    }

    // Wire sub-controllers (called from FXML via fx:include controllers)
    public void setControlPanelController(ControlPanelController c) {
        this.controlPanelController = c;
        c.setReaderController(this);
    }

    public void setTocController(TocController c) {
        this.tocController = c;
        c.setReaderController(this);
    }
}
