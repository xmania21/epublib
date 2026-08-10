package nl.siegmann.epublib.viewer.controller;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.viewer.model.Bookmark;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for {@code /fxml/TocDrawer.fxml}.
 *
 * <p>Renders a slide-in drawer from the left edge containing:</p>
 * <ul>
 *   <li>A {@link ListView} of TOC chapters / spine items</li>
 *   <li>Active chapter highlighted with an amber left-border accent (via CSS)</li>
 * </ul>
 */
public class TocController {

    @FXML private VBox drawerRoot;
    @FXML private ListView<TocEntry> tocList;
    @FXML private ListView<Bookmark> bookmarkList;
    @FXML private TabPane tabPane;

    private ReaderController readerController;
    private int activeIndex = 0;

    private static final Duration SLIDE_DURATION = Duration.millis(260);
    private double drawerWidth = 350;

    @FXML
    public void initialize() {
        drawerRoot.setTranslateX(-drawerWidth);
        drawerRoot.setVisible(false);

        tocList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TocEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("toc-cell-active");
                } else {
                    setText(item.title());
                    if (item.spineIndex() == activeIndex) {
                        getStyleClass().add("toc-cell-active");
                    } else {
                        getStyleClass().removeAll("toc-cell-active");
                    }
                }
            }
        });

        tocList.setOnMouseClicked(event -> {
            TocEntry selected = tocList.getSelectionModel().getSelectedItem();
            if (selected != null && readerController != null) {
                readerController.goToSpineIndex(selected.spineIndex());
                setVisible(false);
            }
        });

        bookmarkList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Bookmark item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });

        bookmarkList.setOnMouseClicked(event -> {
            Bookmark selected = bookmarkList.getSelectionModel().getSelectedItem();
            if (selected != null && readerController != null) {
                readerController.goToSpineIndex(selected.getSpineIndex());
                setVisible(false);
            }
        });
    }

    public void openBookmarksTab() {
        if (tabPane != null) tabPane.getSelectionModel().select(1);
    }

    public void openTocTab() {
        if (tabPane != null) tabPane.getSelectionModel().select(0);
    }

    public void refreshBookmarks(List<Bookmark> bookmarks) {
        bookmarkList.setItems(FXCollections.observableArrayList(bookmarks));
    }

    @FXML
    public void onAddBookmark() {
        if (readerController != null) {
            boolean isBookmarked = readerController.toggleBookmark();
            if (readerController.getControlPanelController() != null) {
                readerController.getControlPanelController().updateBookmarkIcon(isBookmarked);
            }
        }
    }

    /** Populates the TOC list from the book's TOC or spine. */
    public void populateToc(Book book) {
        List<TocEntry> entries = new ArrayList<>();

        // Prefer named TOC references; fall back to spine items
        List<TOCReference> tocRefs = book.getTableOfContents().getTocReferences();
        if (!tocRefs.isEmpty()) {
            List<SpineReference> spineRefs = book.getSpine().getSpineReferences();
            for (TOCReference ref : tocRefs) {
                int idx = findSpineIndex(spineRefs, ref);
                entries.add(new TocEntry(ref.getTitle(), idx));
            }
        } else {
            List<SpineReference> spineRefs = book.getSpine().getSpineReferences();
            for (int i = 0; i < spineRefs.size(); i++) {
                String title = spineRefs.get(i).getResource().getTitle();
                entries.add(new TocEntry(title != null ? title : "Chapter " + (i + 1), i));
            }
        }

        tocList.setItems(FXCollections.observableArrayList(entries));
    }

    private int findSpineIndex(List<SpineReference> spineRefs, TOCReference tocRef) {
        String href = tocRef.getResource() != null ? tocRef.getResource().getHref() : null;
        if (href == null) return 0;
        for (int i = 0; i < spineRefs.size(); i++) {
            if (href.equals(spineRefs.get(i).getResource().getHref())) return i;
        }
        return 0;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            drawerRoot.setVisible(true);
            drawerRoot.setTranslateX(-drawerWidth);
        }
        TranslateTransition tt = new TranslateTransition(SLIDE_DURATION, drawerRoot);
        tt.setToX(visible ? 0 : -drawerWidth);
        if (!visible) tt.setOnFinished(e -> drawerRoot.setVisible(false));
        tt.play();
    }

    @FXML
    private void onClose() { setVisible(false); }

    public void setReaderController(ReaderController rc) { this.readerController = rc; }

    public void setActiveIndex(int index) {
        this.activeIndex = index;
        tocList.refresh();
    }

    /** Lightweight DTO for a TOC entry. */
    public record TocEntry(String title, int spineIndex) {}
}
