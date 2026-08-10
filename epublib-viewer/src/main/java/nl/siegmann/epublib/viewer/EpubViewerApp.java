package nl.siegmann.epublib.viewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.siegmann.epublib.viewer.controller.ReaderController;
import nl.siegmann.epublib.viewer.model.ReadingPreferences;
import nl.siegmann.epublib.viewer.service.PreferencesService;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * JavaFX Application entry point for epublib-viewer.
 *
 * <p>Run on desktop with: {@code mvn javafx:run -pl epublib-viewer}</p>
 * <p>Build for Android/iOS with: {@code mvn gluonfx:build -pl epublib-viewer}</p>
 *
 * <p>Accepts an optional EPUB file path as the first command-line argument.</p>
 */
public class EpubViewerApp extends Application {

    public static final String APP_TITLE = "epublib Reader";
    public static final String VERSION = "5.0-SNAPSHOT";

    private static EpubViewerApp instance;
    private Stage primaryStage;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        this.primaryStage = primaryStage;

        mainScene = new Scene(new StackPane(), 900, 700);
        mainScene.getStylesheets().add(
            getClass().getResource("/styles/app.css").toExternalForm()
        );
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        primaryStage.show();

        // Open EPUB from command line args, or load Library
        List<String> rawParams = getParameters().getRaw();
        if (!rawParams.isEmpty()) {
            File epubFile = new File(rawParams.get(0));
            if (epubFile.exists()) {
                openReader(epubFile);
                return;
            }
        }
        
        openLibrary();
    }

    public static EpubViewerApp getInstance() {
        return instance;
    }

    public void openLibrary() {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/LibraryLayout.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            mainScene.setRoot(loader.load());
            mainScene.getAccelerators().clear();
            primaryStage.setOnCloseRequest(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openReader(File epubFile) {
        try {
            ReadingPreferences prefs = PreferencesService.getInstance().loadPreferences();
            
            URL fxmlUrl = getClass().getResource("/fxml/ReaderLayout.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            StackPane root = loader.load();
            ReaderController controller = loader.getController();
            
            controller.applyPreferences(prefs);
            
            mainScene.setRoot(root);
            mainScene.getAccelerators().clear();
            registerShortcuts(mainScene, controller);
            
            primaryStage.setOnCloseRequest(e -> {
                controller.persistState();
                PreferencesService.getInstance().savePreferences(controller.getCurrentPreferences());
            });
            
            controller.openEpub(epubFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void promptOpenFile(Stage stage, ReaderController controller) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open EPUB");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("EPUB files", "*.epub")
        );
        File chosen = fc.showOpenDialog(stage);
        if (chosen != null) {
            controller.openEpub(chosen);
        }
    }

    private void registerShortcuts(Scene scene, ReaderController controller) {
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN),
            controller::toggleControlPanel
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN),
            controller::toggleToc
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
            controller::openSearchModal
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN),
            controller::toggleTheme
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.RIGHT),
            controller::nextPage
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.LEFT),
            controller::prevPage
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.SPACE),
            controller::nextPage
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.OPEN_BRACKET),
            controller::fontSizeDown
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.CLOSE_BRACKET),
            controller::fontSizeUp
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.ESCAPE),
            controller::dismissOverlays
        );
        
        // Go back to library
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN),
            this::openLibrary
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
