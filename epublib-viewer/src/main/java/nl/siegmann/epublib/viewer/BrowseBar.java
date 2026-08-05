package nl.siegmann.epublib.viewer;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import nl.siegmann.epublib.browsersupport.Navigator;

public class BrowseBar extends JPanel {
	
	private static final long serialVersionUID = -5745389338067538254L;
	private ButtonBar buttonBar;
	
	public BrowseBar(Navigator navigator, ContentPane chapterPane, Viewer viewer) {
		super(new BorderLayout());
		this.buttonBar = new ButtonBar(navigator, chapterPane, viewer);
		add(buttonBar, BorderLayout.CENTER);
		add(new SpineSlider(navigator), BorderLayout.NORTH);
	}

	public void setSelectedReaderTheme(ReaderTheme theme) {
		if (buttonBar != null) {
			buttonBar.setSelectedReaderTheme(theme);
		}
	}
}
