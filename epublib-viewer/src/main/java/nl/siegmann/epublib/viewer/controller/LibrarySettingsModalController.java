package nl.siegmann.epublib.viewer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import nl.siegmann.epublib.viewer.model.ReadingPreferences;
import nl.siegmann.epublib.viewer.service.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibrarySettingsModalController {
    
    private static final Logger log = LoggerFactory.getLogger(LibrarySettingsModalController.class);

    @FXML private StackPane rootPane;
    
    @FXML private Label fontSizeValueLabel;
    @FXML private Slider fontSizeSlider;
    @FXML private Slider lineSpacingSlider;
    @FXML private Slider paragraphSpacingSlider;
    @FXML private Slider letterSpacingSlider;

    private ReadingPreferences prefs;
    private ReaderController readerController;

    public void setReaderController(ReaderController controller) {
        this.readerController = controller;
    }

    @FXML
    public void initialize() {
        prefs = PreferencesService.getInstance().loadPreferences();

        // Initialize sliders with values from prefs
        fontSizeSlider.setValue(prefs.getFontSize());
        lineSpacingSlider.setValue(prefs.getLineHeight());
        paragraphSpacingSlider.setValue(prefs.getParagraphSpacing());
        letterSpacingSlider.setValue(prefs.getLetterSpacing());
        fontSizeValueLabel.setText(prefs.getFontSize() + "pt");

        // Add listeners to save changes immediately
        fontSizeSlider.valueProperty().addListener((obs, old, val) -> {
            int size = val.intValue();
            fontSizeValueLabel.setText(size + "pt");
            prefs.setFontSize(size);
            savePrefs();
        });

        lineSpacingSlider.valueProperty().addListener((obs, old, val) -> {
            prefs.setLineHeight(val.doubleValue());
            savePrefs();
        });

        paragraphSpacingSlider.valueProperty().addListener((obs, old, val) -> {
            prefs.setParagraphSpacing(val.intValue());
            savePrefs();
        });

        letterSpacingSlider.valueProperty().addListener((obs, old, val) -> {
            prefs.setLetterSpacing(val.doubleValue());
            savePrefs();
        });
        
        // consume clicks so they don't propagate underneath
        rootPane.setOnMouseClicked(e -> {
            if (e.getTarget() == rootPane) {
                onClose();
            }
            e.consume();
        });
    }

    private void savePrefs() {
        PreferencesService.getInstance().savePreferences(prefs);
        if (readerController != null) {
            readerController.applyPreferences(prefs);
            readerController.refreshReader();
        }
    }

    public void showModal() {
        // reload prefs in case they changed
        prefs = PreferencesService.getInstance().loadPreferences();
        fontSizeSlider.setValue(prefs.getFontSize());
        lineSpacingSlider.setValue(prefs.getLineHeight());
        paragraphSpacingSlider.setValue(prefs.getParagraphSpacing());
        letterSpacingSlider.setValue(prefs.getLetterSpacing());
        
        rootPane.setVisible(true);
        rootPane.setOpacity(0);
        
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), rootPane);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    public void onClose() {
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), rootPane);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> rootPane.setVisible(false));
        ft.play();
    }
}
