package nl.siegmann.epublib.viewer.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import nl.siegmann.epublib.viewer.EpubViewerApp;
import nl.siegmann.epublib.viewer.model.LibraryBook;
import nl.siegmann.epublib.viewer.service.LibraryService;

import java.io.File;
import java.util.List;

public class LibraryController {

    @FXML private FlowPane bookGrid;
    @FXML private LibrarySettingsModalController settingsModalController;

    @FXML
    public void initialize() {
        refreshGrid();
    }

    private void refreshGrid() {
        bookGrid.getChildren().clear();
        List<LibraryBook> books = LibraryService.getInstance().loadLibrary();
        for (LibraryBook book : books) {
            bookGrid.getChildren().add(createBookCard(book));
        }
    }

    private VBox createBookCard(LibraryBook book) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setStyle("-fx-cursor: hand; -fx-background-radius: 12; -fx-padding: 10; -fx-background-color: transparent;");
        
        // Hover animation for modern feel
        card.hoverProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                card.setStyle("-fx-cursor: hand; -fx-background-radius: 12; -fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 8);");
                card.setScaleX(1.05);
                card.setScaleY(1.05);
            } else {
                card.setStyle("-fx-cursor: hand; -fx-background-radius: 12; -fx-padding: 10; -fx-background-color: transparent;");
                card.setScaleX(1.0);
                card.setScaleY(1.0);
            }
        });

        if (book.getCoverPath() != null && new File(book.getCoverPath()).exists()) {
            ImageView img = new ImageView(new Image(new File(book.getCoverPath()).toURI().toString()));
            img.setFitWidth(140);
            img.setFitHeight(210);
            img.setPreserveRatio(true);
            
            // Clip for rounded corners
            Rectangle clip = new Rectangle(140, 210);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            img.setClip(clip);
            
            card.getChildren().add(img);
        } else {
            Label placeholder = new Label(book.getTitle() != null ? book.getTitle() : "No Title");
            placeholder.setTextFill(Color.WHITE);
            placeholder.setWrapText(true);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setPrefWidth(120);
            placeholder.setStyle("-fx-font-weight: bold;");
            
            VBox rectBox = new VBox(placeholder);
            rectBox.setAlignment(Pos.CENTER);
            rectBox.setMinSize(140, 210);
            rectBox.setMaxSize(140, 210);
            rectBox.setStyle("-fx-background-color: #2a2a3e; -fx-background-radius: 10; -fx-border-color: #3f3f5a; -fx-border-radius: 10; -fx-border-width: 2;");
            card.getChildren().add(rectBox);
        }

        Label title = new Label(book.getTitle() != null ? book.getTitle() : "Unknown Title");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #e8e0d5; -fx-font-size: 14px;");
        title.setMaxWidth(130);

        Label author = new Label(book.getAuthor() != null ? book.getAuthor() : "Unknown Author");
        author.setStyle("-fx-text-fill: #8a8a9e; -fx-font-size: 12px;");
        author.setMaxWidth(130);

        card.getChildren().addAll(title, author);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                File f = new File(book.getFilePath());
                if (f.exists()) {
                    EpubViewerApp.getInstance().openReader(f);
                }
            }
        });

        return card;
    }

    @FXML
    public void onImportEpub() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import EPUB");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB files", "*.epub"));
        File file = fc.showOpenDialog(bookGrid.getScene().getWindow());
        if (file != null) {
            try {
                LibraryService.getInstance().importEpub(file);
                refreshGrid();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onImportFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Import Folder");
        File folder = dc.showDialog(bookGrid.getScene().getWindow());
        if (folder != null) {
            LibraryService.getInstance().importFolder(folder);
            refreshGrid();
        }
    }

    @FXML
    public void onSettingsOpen() {
        if (settingsModalController != null) {
            settingsModalController.showModal();
        }
    }
}
