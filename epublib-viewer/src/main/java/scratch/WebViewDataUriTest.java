package scratch;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WebViewDataUriTest extends Application {
    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        webView.getEngine().loadContent("<html><body><h1>Test</h1></body></html>");
        
        try {
            String css = "body { background-color: red !important; }";
            String dataUri = "data:text/css;charset=utf-8," + URLEncoder.encode(css, StandardCharsets.UTF_8.toString());
            webView.getEngine().setUserStyleSheetLocation(dataUri);
            System.out.println("No exception thrown for data URI.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        stage.setScene(new Scene(webView, 400, 300));
        stage.show();
        
        // Exit after 2 seconds
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (Exception e) {}
            javafx.application.Platform.exit();
        }).start();
    }
    public static void main(String[] args) { launch(args); }
}
