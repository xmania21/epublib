package nl.siegmann.epublib.viewer;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.browsersupport.NavigationEvent;
import nl.siegmann.epublib.browsersupport.NavigationEventListener;
import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.util.DesktopUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a page
 * 
 */
public class ContentPane extends JPanel implements NavigationEventListener,
		HyperlinkListener {

	private static final long serialVersionUID = -5322988066178102320L;

	private static final Logger log = LoggerFactory
			.getLogger(ContentPane.class);
	private Navigator navigator;
	private Resource currentResource;
	private JEditorPane editorPane;
	private JScrollPane scrollPane;
	private HTMLDocumentFactory htmlDocumentFactory;
	private ReaderTheme currentReaderTheme = ReaderTheme.LIGHT;
	
	public ContentPane(Navigator navigator) {
		super(new GridLayout(1, 0));
		this.scrollPane = (JScrollPane) add(new JScrollPane());
		this.scrollPane.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					Point viewPosition = scrollPane.getViewport().getViewPosition();
					int newY = (int) (viewPosition.getY() + 10);
					scrollPane.getViewport().setViewPosition(new Point((int) viewPosition.getX(), newY));
				}
			}
		});
		this.scrollPane.addMouseWheelListener(new MouseWheelListener() {
			
			private boolean gotoNextPage = false;
			private boolean gotoPreviousPage = false;

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
			    int notches = e.getWheelRotation();
			    int increment = scrollPane.getVerticalScrollBar().getUnitIncrement(1);
			    if (notches < 0) {
					Point viewPosition = scrollPane.getViewport().getViewPosition();
					if (viewPosition.getY() - increment < 0) {
						if (gotoPreviousPage) {
							gotoPreviousPage = false;
							ContentPane.this.navigator.gotoPreviousSpineSection(-1, ContentPane.this);
						} else {
							gotoPreviousPage = true;
							scrollPane.getViewport().setViewPosition(new Point((int) viewPosition.getX(), 0));
						}
					}
			    } else {
			    	// only move to the next page if we are exactly at the bottom of the current page
			    	Point viewPosition = scrollPane.getViewport().getViewPosition();
					int viewportHeight = scrollPane.getViewport().getHeight();
					int scrollMax = scrollPane.getVerticalScrollBar().getMaximum();
					if (viewPosition.getY() + viewportHeight + increment > scrollMax) {
						if (gotoNextPage) {
							gotoNextPage = false;
							ContentPane.this.navigator.gotoNextSpineSection(ContentPane.this);
						} else {
							gotoNextPage = true;
							int newY = scrollMax - viewportHeight;
							scrollPane.getViewport().setViewPosition(new Point((int) viewPosition.getX(), newY));
						}
					}
			    }
			  }
		});
		this.navigator = navigator;
		navigator.addNavigationEventListener(this);
		this.editorPane = createJEditorPane();
		scrollPane.getViewport().add(editorPane);
		this.htmlDocumentFactory = new HTMLDocumentFactory(navigator, editorPane.getEditorKit());
		initBook(navigator.getBook());
	}

	private void initBook(Book book) {
		if (book == null) {
			return;
		}
		htmlDocumentFactory.init(book);
		displayPage(book.getCoverPage());
	}
	
	
	
	/**
	 * Whether the given searchString matches any of the possibleValues.
	 * 
	 * @param searchString
	 * @param possibleValues
	 * @return Whether the given searchString matches any of the possibleValues.
	 */
	private static boolean matchesAny(String searchString, String... possibleValues) {
		for (int i = 0; i < possibleValues.length; i++) {
			String attributeValue = possibleValues[i];
			if (StringUtils.isNotBlank(attributeValue) && (attributeValue.equals(searchString))) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Scrolls the editorPane to the startOffset of the current element in the elementIterator
	 * 
	 * @param requestFragmentId
	 * @param attributeValue
	 * @param editorPane
	 * @param elementIterator
	 * 
	 * @return whether it was a match and we jumped there.
	 */
	private static void scrollToElement(JEditorPane editorPane, HTMLDocument.Iterator elementIterator) {
		try {
			java.awt.geom.Rectangle2D rect2D = editorPane.modelToView2D(elementIterator.getStartOffset());
			if (rect2D == null) {
				return;
			}
			Rectangle rectangle = rect2D.getBounds();
			// the view is visible, scroll it to the
			// center of the current visible area.
			Rectangle visibleRectangle = editorPane.getVisibleRect();
			// r.y -= (vis.height / 2);
			rectangle.height = visibleRectangle.height;
			editorPane.scrollRectToVisible(rectangle);
		} catch (BadLocationException e) {
			log.error(e.getMessage());
		}
	}
	
	
	/**
	 * Scrolls the editorPane to the first anchor element whose id or name matches the given fragmentId.
	 * 
	 * @param fragmentId
	 */
	private void scrollToNamedAnchor(String fragmentId) {
		HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
		for (HTMLDocument.Iterator iter = doc.getIterator(HTML.Tag.A); iter.isValid(); iter.next()) {
			AttributeSet attributes = iter.getAttributes();
			if (matchesAny(fragmentId, (String) attributes.getAttribute(HTML.Attribute.NAME),
					(String) attributes.getAttribute(HTML.Attribute.ID))) {
				scrollToElement(editorPane, iter);
				break;
			}
		}
	}

	private JEditorPane createJEditorPane() {
		JEditorPane editorPane = new JEditorPane() {
			@Override
			public void updateUI() {
				super.updateUI();
				if (currentReaderTheme != null) {
					setBackground(currentReaderTheme.getBackgroundColor());
					setForeground(currentReaderTheme.getTextColor());
				}
			}
		};
		editorPane.setBackground(currentReaderTheme.getBackgroundColor());
		editorPane.setForeground(currentReaderTheme.getTextColor());
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.FALSE);
		editorPane.setEditable(false);
		HTMLEditorKit htmlKit = new HTMLEditorKit();
		editorPane.setEditorKit(htmlKit);
		editorPane.addHyperlinkListener(this);
		editorPane.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent keyEvent) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
					navigator.gotoNextSpineSection(ContentPane.this);
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
					navigator.gotoPreviousSpineSection(ContentPane.this);
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE) {
					ContentPane.this.gotoNextPage();
				}
			}
		});
		return editorPane;
	}

	private int fontSizePt = 14;
	private String fontFamily = "Sans-Serif";

	public void zoomIn() {
		if (fontSizePt < 36) {
			fontSizePt += 2;
			applyReaderTheme(currentReaderTheme);
		}
	}

	public void zoomOut() {
		if (fontSizePt > 8) {
			fontSizePt -= 2;
			applyReaderTheme(currentReaderTheme);
		}
	}

	public void resetZoom() {
		fontSizePt = 14;
		applyReaderTheme(currentReaderTheme);
	}

	public void setFontFamily(String family) {
		if (family != null && !family.isBlank()) {
			this.fontFamily = family;
			applyReaderTheme(currentReaderTheme);
		}
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (currentReaderTheme != null) {
			setBackground(currentReaderTheme.getBackgroundColor());
			if (scrollPane != null) {
				scrollPane.setBackground(currentReaderTheme.getBackgroundColor());
				if (scrollPane.getViewport() != null) {
					scrollPane.getViewport().setBackground(currentReaderTheme.getBackgroundColor());
				}
			}
		}
	}

	public void applyReaderTheme(ReaderTheme theme) {
		if (theme == null) {
			return;
		}
		boolean themeChanged = (this.currentReaderTheme != theme);
		this.currentReaderTheme = theme;
		Color bg = theme.getBackgroundColor();
		Color fg = theme.getTextColor();

		setBackground(bg);
		if (scrollPane != null) {
			scrollPane.setBackground(bg);
			if (scrollPane.getViewport() != null) {
				scrollPane.getViewport().setBackground(bg);
			}
		}
		if (editorPane != null) {
			editorPane.setBackground(bg);
			editorPane.setForeground(fg);
			editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.FALSE);
			
			String bgHex = theme.getBgHex();
			String textHex = theme.getTextHex();
			String linkHex = theme.getLinkHex();

			String rootRule = "html, body { background-color: " + bgHex + "; color: " + textHex + "; font-size: " + fontSizePt + "pt; font-family: " + fontFamily + "; }";
			String blockRule = "p, div, td, th, tr, table, thead, tbody, tfoot, ul, ol, li, dl, dt, dd, h1, h2, h3, h4, h5, h6, blockquote, pre, code, section, article, header, footer, nav, main, figure, figcaption, form, hr, implied { color: " + textHex + "; background-color: " + bgHex + "; font-size: " + fontSizePt + "pt; font-family: " + fontFamily + "; }";
			String inlineRule = "span, em, strong, b, i, u, s, small, mark, sub, sup { color: " + textHex + "; font-size: " + fontSizePt + "pt; font-family: " + fontFamily + "; }";
			String linkRule = "a, a:link, a:visited, a:hover, a:active { color: " + linkHex + "; }";

			HTMLEditorKit htmlKit = (HTMLEditorKit) editorPane.getEditorKit();
			if (htmlKit != null) {
				StyleSheet styleSheet = htmlKit.getStyleSheet();
				styleSheet.addRule(rootRule);
				styleSheet.addRule(blockRule);
				styleSheet.addRule(inlineRule);
				styleSheet.addRule(linkRule);
			}

			if (themeChanged && htmlDocumentFactory != null && currentResource != null) {
				htmlDocumentFactory.clearCache();
				HTMLDocument freshDoc = htmlDocumentFactory.getDocument(currentResource);
				if (freshDoc != null) {
					editorPane.setDocument(freshDoc);
				}
			}

			if (editorPane.getDocument() instanceof HTMLDocument htmlDoc) {
				StyleSheet docStyleSheet = htmlDoc.getStyleSheet();
				docStyleSheet.addRule(rootRule);
				docStyleSheet.addRule(blockRule);
				docStyleSheet.addRule(inlineRule);
				docStyleSheet.addRule(linkRule);
			}

			editorPane.repaint();
		}
	}

	public void displayPage(Resource resource) {
		displayPage(resource, 0);
	}

	public void displayPage(Resource resource, int sectionPos) {
		if (resource == null) {
			return;
		}
		try {
			HTMLDocument document = htmlDocumentFactory.getDocument(resource);
			if (document == null) {
				return;
			}
			currentResource = resource;
			editorPane.setDocument(document);
			applyReaderTheme(currentReaderTheme);
			scrollToCurrentPosition(sectionPos);
		} catch (Exception e) {
			log.error("When reading resource " + resource.getId() + "("
					+ resource.getHref() + ") :" + e.getMessage(), e);
		}
	}

	private void scrollToCurrentPosition(int sectionPos) {
		if (sectionPos < 0) {
			editorPane.setCaretPosition(editorPane.getDocument().getLength());
		} else {
			editorPane.setCaretPosition(sectionPos);
		}
		if (sectionPos == 0) {
			scrollPane.getViewport().setViewPosition(new Point(0, 0));
		} else if (sectionPos < 0) {
			int viewportHeight = scrollPane.getViewport().getHeight();
			int scrollMax = scrollPane.getVerticalScrollBar().getMaximum();
			scrollPane.getViewport().setViewPosition(new Point(0, scrollMax - viewportHeight));
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
			return;
		}
        final URL url = event.getURL();
        if (url.getProtocol().toLowerCase().startsWith("http") && !"".equals(url.getHost())) {
            try {
                DesktopUtil.launchBrowser(event.getURL());
                return;
            } catch (DesktopUtil.BrowserLaunchException ex) {
                log.warn("Couldn't launch system web browser.", ex);
            }
        }
		String resourceHref = calculateTargetHref(event.getURL());
		if (resourceHref.startsWith("#")) {
			scrollToNamedAnchor(resourceHref.substring(1));
			return;
		}

		Resource resource = navigator.getBook().getResources().getByHref(resourceHref);
		if (resource == null) {
			log.error("Resource with url " + resourceHref + " not found");
		} else {
			navigator.gotoResource(resource, this);
		}
	}

	public void gotoPreviousPage() {
		Point viewPosition = scrollPane.getViewport().getViewPosition();
		if (viewPosition.getY() <= 0) {
			navigator.gotoPreviousSpineSection(this);
			return;
		}
		int viewportHeight = scrollPane.getViewport().getHeight();
		int newY = (int) viewPosition.getY();
		newY -= viewportHeight;
		newY = Math.max(0, newY - viewportHeight);
		scrollPane.getViewport().setViewPosition(
				new Point((int) viewPosition.getX(), newY));
	}

	public void gotoNextPage() {
		Point viewPosition = scrollPane.getViewport().getViewPosition();
		int viewportHeight = scrollPane.getViewport().getHeight();
		int scrollMax = scrollPane.getVerticalScrollBar().getMaximum();
		if (viewPosition.getY() + viewportHeight >= scrollMax) {
			navigator.gotoNextSpineSection(this);
			return;
		}
		int newY = ((int) viewPosition.getY()) + viewportHeight;
		scrollPane.getViewport().setViewPosition(
				new Point((int) viewPosition.getX(), newY));
	}

	
	/**
	 * Transforms a link generated by a click on a link in a document to a resource href.
	 * Property handles http encoded spaces and such.
	 * 
	 * @param clickUrl
	 * @return a link generated by a click on a link transformed into a document to a resource href.
	 */
	private String calculateTargetHref(URL clickUrl) {
		String resourceHref = clickUrl.toString();
		try {
			resourceHref = URLDecoder.decode(resourceHref,
					Constants.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		resourceHref = resourceHref.substring(ImageLoaderCache.IMAGE_URL_PREFIX
				.length());

		if (resourceHref.startsWith("#")) {
			return resourceHref;
		}
		if (currentResource != null
				&& StringUtils.isNotBlank(currentResource.getHref())) {
			int lastSlashPos = currentResource.getHref().lastIndexOf('/');
			if (lastSlashPos >= 0) {
				resourceHref = currentResource.getHref().substring(0,
						lastSlashPos + 1)
						+ resourceHref;
			}
		}
		return resourceHref;
	}

	
	public void navigationPerformed(NavigationEvent navigationEvent) {
		if (navigationEvent.isBookChanged()) {
			initBook(navigationEvent.getCurrentBook());
		} else {
			if (navigationEvent.isResourceChanged()) {
			displayPage(navigationEvent.getCurrentResource(),
					navigationEvent.getCurrentSectionPos());
			} else if (navigationEvent.isSectionPosChanged()) {
				editorPane.setCaretPosition(navigationEvent.getCurrentSectionPos());
			}
			if (StringUtils.isNotBlank(navigationEvent.getCurrentFragmentId())) {
				scrollToNamedAnchor(navigationEvent.getCurrentFragmentId());
			}
		}
	}


}
