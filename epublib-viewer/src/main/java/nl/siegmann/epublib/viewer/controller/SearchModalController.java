package nl.siegmann.epublib.viewer.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nl.siegmann.epublib.search.SearchIndex;
import nl.siegmann.epublib.search.SearchResult;
import nl.siegmann.epublib.search.SearchResults;

public class SearchModalController {

    @FXML private StackPane modalRoot;
    @FXML private VBox modalContent;
    @FXML private TextField searchInput;
    @FXML private Button searchBtn;
    @FXML private ListView<SearchResult> resultsListView;
    @FXML private VBox loadingBox;
    @FXML private VBox emptyStateBox;

    private ReaderController readerController;
    private SearchIndex searchIndex;

    public void setReaderController(ReaderController readerController) {
        this.readerController = readerController;
    }

    public void setSearchIndex(SearchIndex searchIndex) {
        this.searchIndex = searchIndex;
    }

    @FXML
    public void initialize() {
        emptyStateBox.setVisible(false);
        loadingBox.setVisible(false);
        resultsListView.setVisible(false);
        
        resultsListView.setCellFactory(lv -> new ListCell<SearchResult>() {
            @Override
            protected void updateItem(SearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String title = item.getResource().getTitle();
                    if (title == null || title.isEmpty()) {
                        title = item.getResource().getHref();
                    }
                    
                    String context = item.getContextText();
                    String term = searchInput.getText();
                    
                    javafx.scene.text.Text titleText = new javafx.scene.text.Text(title + " — ");
                    titleText.setStyle("-fx-font-weight: bold; -fx-fill: -fx-text-muted;");
                    
                    javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow(titleText);
                    
                    if (term != null && !term.trim().isEmpty() && context != null) {
                        String lowerContext = context.toLowerCase();
                        String lowerTerm = term.trim().toLowerCase();
                        int termLen = lowerTerm.length();
                        
                        int lastIndex = 0;
                        int index = lowerContext.indexOf(lowerTerm);
                        while (index >= 0) {
                            if (index > lastIndex) {
                                javafx.scene.text.Text normalText = new javafx.scene.text.Text(context.substring(lastIndex, index));
                                normalText.getStyleClass().add("search-result-text");
                                flow.getChildren().add(normalText);
                            }
                            
                            javafx.scene.text.Text highlightText = new javafx.scene.text.Text(context.substring(index, index + termLen));
                            highlightText.getStyleClass().add("search-result-highlight");
                            flow.getChildren().add(highlightText);
                            
                            lastIndex = index + termLen;
                            index = lowerContext.indexOf(lowerTerm, lastIndex);
                        }
                        
                        if (lastIndex < context.length()) {
                            javafx.scene.text.Text normalText = new javafx.scene.text.Text(context.substring(lastIndex));
                            normalText.getStyleClass().add("search-result-text");
                            flow.getChildren().add(normalText);
                        }
                    } else {
                        javafx.scene.text.Text normalText = new javafx.scene.text.Text(context != null ? context : "");
                        normalText.getStyleClass().add("search-result-text");
                        flow.getChildren().add(normalText);
                    }
                    
                    setText(null);
                    setGraphic(flow);
                }
            }
        });

        resultsListView.setOnMouseClicked(e -> {
            SearchResult item = resultsListView.getSelectionModel().getSelectedItem();
            if (item != null && readerController != null) {
                readerController.jumpToSearchResult(item);
                closeModal();
            }
        });
    }

    @FXML
    public void onSearchAction() {
        String term = searchInput.getText();
        if (term == null || term.trim().isEmpty() || searchIndex == null) {
            return;
        }

        resultsListView.setVisible(false);
        emptyStateBox.setVisible(false);
        loadingBox.setVisible(true);

        Task<SearchResults> searchTask = new Task<SearchResults>() {
            @Override
            protected SearchResults call() throws Exception {
                return searchIndex.doSearch(term.trim());
            }
        };

        searchTask.setOnSucceeded(e -> {
            loadingBox.setVisible(false);
            SearchResults results = searchTask.getValue();
            if (results == null || results.isEmpty()) {
                emptyStateBox.setVisible(true);
            } else {
                resultsListView.getItems().setAll(results.getHits());
                resultsListView.setVisible(true);
            }
        });

        searchTask.setOnFailed(e -> {
            loadingBox.setVisible(false);
            emptyStateBox.setVisible(true);
            searchTask.getException().printStackTrace();
        });

        Thread searchThread = new Thread(searchTask);
        searchThread.setContextClassLoader(getClass().getClassLoader());
        searchThread.start();
    }

    @FXML
    public void onClose() {
        closeModal();
    }

    @FXML
    public void onOverlayClicked(javafx.scene.input.MouseEvent event) {
        closeModal();
    }

    @FXML
    public void onContentClicked(javafx.scene.input.MouseEvent event) {
        // Prevent closing when clicking inside the content box
        event.consume();
    }

    public void showModal() {
        modalRoot.setVisible(true);
        Platform.runLater(() -> searchInput.requestFocus());
    }

    public void closeModal() {
        modalRoot.setVisible(false);
    }
}
