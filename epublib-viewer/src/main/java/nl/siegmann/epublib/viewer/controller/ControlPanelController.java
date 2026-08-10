package nl.siegmann.epublib.viewer.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nl.siegmann.epublib.viewer.model.ReadingPreferences;
import nl.siegmann.epublib.viewer.model.ReadingTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlPanelController {
    
    private static final Logger log = LoggerFactory.getLogger(ControlPanelController.class);

    // ---- FXML nodes ----
    @FXML private VBox panelRoot;
    @FXML private Label chapterLabel;
    @FXML private Label pageCounterLabel;
    @FXML private ProgressBar chapterProgress;
    
    @FXML private Label currentChapterLabel;
    @FXML private Label totalChaptersLabel;
    
    @FXML private Label headerTitleLabel;
    
    @FXML private Button themeLightBtn;
    @FXML private Button themeSepiaBtn;
    @FXML private Button themeDarkBtn;


    @FXML private Label fontSizeValueLabel;
    @FXML private Slider fontSizeSlider;
    @FXML private Slider marginTopSlider;
    @FXML private Slider marginBottomSlider;
    @FXML private Slider marginLeftSlider;
    @FXML private Slider marginRightSlider;
    @FXML private Slider lineSpacingSlider;
    @FXML private Slider paragraphSpacingSlider;
    @FXML private Slider letterSpacingSlider;
    @FXML private Button bookmarkBtn;
    @FXML private ToggleButton boldToggle;
    @FXML private ToggleButton italicToggle;
    @FXML private ToggleButton underlineToggle;
    @FXML private ToggleGroup alignGroup;
    @FXML private ToggleButton alignLeftToggle;
    @FXML private ToggleButton alignJustifyToggle;

    @FXML private Button themeSolarizedBtn;
    @FXML private Button themeNightBtn;
    @FXML private Button themeOledBtn;

    @FXML private Slider chapterScrubber;

    // ---- Back-reference to parent ----
    private ReaderController readerController;
    private ReadingPreferences prefs;

    private static final Duration SLIDE_DURATION = Duration.millis(280);
    private double panelHeight = 350;   // approximate; refined after layout
    private boolean isVisible = false;

    @FXML
    public void initialize() {
        panelRoot.setVisible(false);
        panelRoot.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                panelHeight = newVal.doubleValue();
                if (!isVisible) {
                    panelRoot.setTranslateY(panelHeight);
                }
            }
        });

        // Font size slider: 12–72, integer steps
        fontSizeSlider.setMin(12);
        fontSizeSlider.setMax(72);
        fontSizeSlider.setMajorTickUnit(1);
        fontSizeSlider.setSnapToTicks(true);
        fontSizeSlider.valueProperty().addListener((obs, old, val) -> {
            int size = val.intValue();
            log.debug("Font Size changed to: {}", size);
            fontSizeValueLabel.setText(size + "pt");
            if (prefs != null) {
                prefs.setFontSize(size);
                if (readerController != null) readerController.refreshReader();
            }
        });

        // Margin sliders
        marginTopSlider.valueProperty().addListener((obs, old, val) -> { if (prefs != null) { prefs.setMarginTop(val.intValue()); if (readerController != null) readerController.refreshReader(); } });
        marginBottomSlider.valueProperty().addListener((obs, old, val) -> { if (prefs != null) { prefs.setMarginBottom(val.intValue()); if (readerController != null) readerController.refreshReader(); } });
        marginLeftSlider.valueProperty().addListener((obs, old, val) -> { if (prefs != null) { prefs.setMarginLeft(val.intValue()); if (readerController != null) readerController.refreshReader(); } });
        marginRightSlider.valueProperty().addListener((obs, old, val) -> { if (prefs != null) { prefs.setMarginRight(val.intValue()); if (readerController != null) readerController.refreshReader(); } });

        // Line Spacing slider
        lineSpacingSlider.valueProperty().addListener((obs, old, val) -> {
            if (prefs != null) {
                prefs.setLineHeight(val.doubleValue());
                if (readerController != null) readerController.refreshReader();
            }
        });

        // Paragraph Spacing slider
        paragraphSpacingSlider.valueProperty().addListener((obs, old, val) -> { if (prefs != null) { prefs.setParagraphSpacing(val.intValue()); if (readerController != null) readerController.refreshReader(); } });

        // Letter Spacing slider
        letterSpacingSlider.valueProperty().addListener((obs, old, val) -> { if (prefs != null) { prefs.setLetterSpacing(val.doubleValue()); if (readerController != null) readerController.refreshReader(); } });

        // Typography Toggles
        boldToggle.setOnAction(e -> {
            if (prefs != null) {
                prefs.setBold(boldToggle.isSelected());
                if (readerController != null) readerController.refreshReader();
            }
        });
        
        italicToggle.setOnAction(e -> {
            if (prefs != null) {
                prefs.setItalic(italicToggle.isSelected());
                if (readerController != null) readerController.refreshReader();
            }
        });

        underlineToggle.setOnAction(e -> {
            if (prefs != null) {
                prefs.setUnderline(underlineToggle.isSelected());
                if (readerController != null) readerController.refreshReader();
            }
        });

        alignGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (prefs != null && val != null) {
                String align = (val == alignJustifyToggle) ? "justify" : "left";
                prefs.setTextAlign(align);
                if (readerController != null) readerController.refreshReader();
            }
        });

        themeLightBtn.setOnAction(e -> setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme.LIGHT));
        themeSepiaBtn.setOnAction(e -> setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme.SEPIA));
        themeDarkBtn.setOnAction(e -> setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme.DARK));
        themeNightBtn.setOnAction(e -> setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme.NIGHT));
        themeOledBtn.setOnAction(e -> setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme.OLED_BLACK));
        themeSolarizedBtn.setOnAction(e -> setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme.SOLARIZED_LIGHT));



        // Chapter scrubber
        chapterScrubber.setOnMouseReleased(e -> {
            if (readerController != null) {
                readerController.goToSpineIndex((int) chapterScrubber.getValue());
            }
        });
        chapterScrubber.valueProperty().addListener((obs, old, val) -> {
            if (currentChapterLabel != null) {
                currentChapterLabel.setText(String.valueOf(val.intValue() + 1));
            }
        });
    }

    private void setTheme(nl.siegmann.epublib.viewer.model.ReadingTheme theme) {
        log.debug("Setting theme to: {}", theme);
        if (prefs != null) {
            prefs.setTheme(theme);
            if (readerController != null) {
                readerController.refreshReader();
            } else {
                log.debug("readerController is null in setTheme!");
            }
        } else {
            log.debug("prefs is null in setTheme!");
        }
    }

    // =========================================================================
    // Slide animation
    // =========================================================================

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if (visible) {
            panelRoot.setVisible(true);
            panelRoot.setTranslateY(panelHeight);
        }
        TranslateTransition tt = new TranslateTransition(SLIDE_DURATION, panelRoot);
        tt.setToY(visible ? 0 : panelHeight);
        if (!visible) {
            tt.setOnFinished(e -> panelRoot.setVisible(false));
        }
        tt.play();
    }
    
    public boolean isPanelVisible() {
        return isVisible;
    }

    // =========================================================================
    // State update
    // =========================================================================

    public void updateProgress(int spineIndex, int totalChapters, String chapterTitle) {
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(chapterTitle);
        }
        chapterLabel.setText("≡ " + chapterTitle);
        pageCounterLabel.setText((spineIndex + 1) + " / " + totalChapters);
        chapterProgress.setProgress((double) (spineIndex + 1) / totalChapters);
        chapterScrubber.setMax(totalChapters - 1);
        chapterScrubber.setValue(spineIndex);
        if (currentChapterLabel != null) {
            currentChapterLabel.setText(String.valueOf(spineIndex + 1));
        }
        if (totalChaptersLabel != null) {
            totalChaptersLabel.setText(String.valueOf(totalChapters));
        }
        if (readerController != null) {
            updateBookmarkIcon(readerController.isCurrentPageBookmarked());
        }
    }

    public void applyPreferences(ReadingPreferences prefs) {
        this.prefs = prefs;
        fontSizeSlider.setValue(prefs.getFontSize());
        marginTopSlider.setValue(prefs.getMarginTop());
        marginBottomSlider.setValue(prefs.getMarginBottom());
        marginLeftSlider.setValue(prefs.getMarginLeft());
        marginRightSlider.setValue(prefs.getMarginRight());
        lineSpacingSlider.setValue(prefs.getLineHeight());
        paragraphSpacingSlider.setValue(prefs.getParagraphSpacing());
        letterSpacingSlider.setValue(prefs.getLetterSpacing());
        boldToggle.setSelected(prefs.isBold());
        italicToggle.setSelected(prefs.isItalic());
        underlineToggle.setSelected(prefs.isUnderline());
        if ("justify".equals(prefs.getTextAlign())) {
            alignGroup.selectToggle(alignJustifyToggle);
        } else {
            alignGroup.selectToggle(alignLeftToggle);
        }

        fontSizeValueLabel.setText(prefs.getFontSize() + "pt");
    }

    // =========================================================================
    // Quick-action button handlers
    // =========================================================================



    @FXML
    private void onSearchOpen() {
        if (readerController != null) {
            readerController.openSearchModal();
        }
    }

    @FXML
    private void onBackToLibrary() {
        if (readerController != null) {
            readerController.persistState();
            nl.siegmann.epublib.viewer.service.PreferencesService.getInstance().savePreferences(readerController.getCurrentPreferences());
            nl.siegmann.epublib.viewer.EpubViewerApp.getInstance().openLibrary();
        }
    }

    @FXML
    private void onSettingsOpen() {
        if (readerController != null) {
            readerController.openSettingsModal();
        }
    }

    @FXML
    private void onBookmarkToggle() {
        if (readerController != null) {
            boolean isBookmarked = readerController.toggleBookmark();
            updateBookmarkIcon(isBookmarked);
        }
    }

    public void updateBookmarkIcon(boolean isBookmarked) {
        if (bookmarkBtn != null) {
            bookmarkBtn.setText(isBookmarked ? "★" : "☆");
            bookmarkBtn.setStyle(isBookmarked ? "-fx-text-fill: -fx-accent;" : "");
        }
    }

    @FXML
    private void onTocOpen() {
        if (readerController != null) readerController.toggleToc();
    }

    public void setReaderController(ReaderController controller) {
        this.readerController = controller;
        applyPreferences(controller.getCurrentPreferences());
    }
}
