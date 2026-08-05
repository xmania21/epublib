package nl.siegmann.epublib.viewer;

import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewerApp {

	private static final Logger log = LoggerFactory.getLogger(ViewerApp.class);

	private static InputStream getBookInputStream(String[] args) {
		String bookFile = (args.length > 0) ? args[0] : null;
		InputStream result = null;
		if (!StringUtils.isBlank(bookFile)) {
			try {
				result = new FileInputStream(bookFile);
			} catch (Exception e) {
				log.error("Unable to open {}", bookFile, e);
			}
		}
		if (result == null) {
			result = ViewerApp.class.getResourceAsStream("/viewer/epublibviewer-help.epub");
		}
		return result;
	}

	public static void main(String[] args) {
		try {
			FlatLightLaf.setup();
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				log.error("Unable to set look and feel", ex);
			}
		}

		final InputStream bookStream = getBookInputStream(args);
		SwingUtilities.invokeLater(() -> new Viewer(bookStream));
	}
}
