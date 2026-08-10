package nl.siegmann.epublib.viewer.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nl.siegmann.epublib.viewer.model.ReadingPreferences;
import nl.siegmann.epublib.viewer.model.TapZonePreference;

/**
 * Controller for {@code /fxml/TapZoneSettings.fxml}.
 *
 * <p>Renders visual diagrams of the available tap zone presets using JavaFX
 * {@link Canvas} and binds the selection to {@link ReadingPreferences}.</p>
 */
public class TapZoneSettingsController {

    @FXML private VBox settingsRoot;
    @FXML private ToggleGroup layoutGroup;
    @FXML private RadioButton rbClassic;
    @FXML private RadioButton rbSplit;
    @FXML private RadioButton rbKindle;
    @FXML private RadioButton rbTopBottom;

    @FXML private Canvas cvsClassic;
    @FXML private Canvas cvsSplit;
    @FXML private Canvas cvsKindle;
    @FXML private Canvas cvsTopBottom;

    private ReadingPreferences prefs;

    @FXML
    public void initialize() {
        drawLayout(cvsClassic, TapZonePreference.CLASSIC_LCR);
        drawLayout(cvsSplit, TapZonePreference.SPLIT_LR);
        drawLayout(cvsKindle, TapZonePreference.KINDLE_STYLE);
        drawLayout(cvsTopBottom, TapZonePreference.TOP_BOTTOM);

        rbClassic.setUserData(TapZonePreference.CLASSIC_LCR);
        rbSplit.setUserData(TapZonePreference.SPLIT_LR);
        rbKindle.setUserData(TapZonePreference.KINDLE_STYLE);
        rbTopBottom.setUserData(TapZonePreference.TOP_BOTTOM);

        layoutGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val != null && prefs != null) {
                prefs.setTapZone((TapZonePreference) val.getUserData());
            }
        });
    }

    public void setPreferences(ReadingPreferences prefs) {
        this.prefs = prefs;
        switch (prefs.getTapZone()) {
            case CLASSIC_LCR  -> layoutGroup.selectToggle(rbClassic);
            case SPLIT_LR     -> layoutGroup.selectToggle(rbSplit);
            case KINDLE_STYLE -> layoutGroup.selectToggle(rbKindle);
            case TOP_BOTTOM   -> layoutGroup.selectToggle(rbTopBottom);
        }
    }

    /** Draws a miniature representation of the tap zone layout. */
    private void drawLayout(Canvas canvas, TapZonePreference pref) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Background
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, w, h);

        // Draw regions by sampling a grid
        int cols = 20;
        int rows = 20;
        double cellW = w / cols;
        double cellH = h / rows;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                double px = x * cellW + (cellW / 2);
                double py = y * cellH + (cellH / 2);

                TapZonePreference.TapAction action = pref.resolve(px, py, w, h);
                switch (action) {
                    case PREV     -> gc.setFill(Color.web("#3b82f6", 0.6)); // Blue
                    case NEXT     -> gc.setFill(Color.web("#10b981", 0.6)); // Green
                    case SETTINGS -> gc.setFill(Color.web("#f59e0b", 0.6)); // Amber
                }
                gc.fillRect(x * cellW, y * cellH, cellW, cellH);
            }
        }

        // Draw borders
        gc.setStroke(Color.web("#ffffff", 0.1));
        gc.setLineWidth(1.0);
        gc.strokeRect(0, 0, w, h);
    }
}
