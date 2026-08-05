package nl.siegmann.epublib.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Viewer {
	
	static final Logger log = LoggerFactory.getLogger(Viewer.class);
	private final JFrame mainWindow;
	private BrowseBar browseBar;
	private JSplitPane mainSplitPane; 
	private JSplitPane rightSplitPane;
	private ContentPane htmlPane;
	private UiTheme currentUiTheme = UiTheme.LIGHT;
	private ReaderTheme currentReaderTheme = ReaderTheme.LIGHT;
	private Map<ReaderTheme, JRadioButtonMenuItem> readerThemeMenuItems = new HashMap<>();
	private Navigator navigator = new Navigator();
	
	private JTabbedPane leftTabbedPane;
	private BookmarkPane bookmarkPane;
	private boolean isFullScreen = false;
	private final Preferences prefs = Preferences.userNodeForPackage(Viewer.class);
	private JMenu recentFilesMenu;

	public Viewer(InputStream bookStream) {
		mainWindow = createMainWindow();
		Book book;
		try {
			book = (new EpubReader()).readEpub(bookStream);
			gotoBook(book);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public Viewer(Book book) {
		mainWindow = createMainWindow();
		gotoBook(book);
	}

	public void setUiTheme(UiTheme theme) {
		if (theme == null) {
			return;
		}
		this.currentUiTheme = theme;
		theme.apply();
		if (htmlPane != null) {
			htmlPane.applyReaderTheme(currentReaderTheme);
		}
	}

	public void setReaderTheme(ReaderTheme theme) {
		if (theme == null) {
			return;
		}
		this.currentReaderTheme = theme;
		if (htmlPane != null) {
			htmlPane.applyReaderTheme(theme);
		}
		if (browseBar != null) {
			browseBar.setSelectedReaderTheme(theme);
		}
		JRadioButtonMenuItem menuItem = readerThemeMenuItems.get(theme);
		if (menuItem != null && !menuItem.isSelected()) {
			menuItem.setSelected(true);
		}
	}

	private void openFile(File selectedFile) {
		if (selectedFile == null || !selectedFile.exists()) {
			return;
		}
		try {
			Book book = (new EpubReader()).readEpub(new FileInputStream(selectedFile));
			gotoBook(book);
			saveRecentFile(selectedFile.getAbsolutePath());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		}
	}

	private void saveRecentFile(String path) {
		String recent = prefs.get("recent_files", "");
		List<String> list = new ArrayList<>();
		list.add(path);
		for (String p : recent.split(";")) {
			if (!p.isBlank() && !p.equalsIgnoreCase(path) && list.size() < 5) {
				list.add(p);
			}
		}
		prefs.put("recent_files", String.join(";", list));
		updateRecentFilesMenu();
	}

	private void updateRecentFilesMenu() {
		if (recentFilesMenu == null) {
			return;
		}
		recentFilesMenu.removeAll();
		String recent = prefs.get("recent_files", "");
		if (recent.isBlank()) {
			JMenuItem emptyItem = new JMenuItem("(No recent files)");
			emptyItem.setEnabled(false);
			recentFilesMenu.add(emptyItem);
			return;
		}
		for (String path : recent.split(";")) {
			if (!path.isBlank()) {
				File f = new File(path);
				JMenuItem item = new JMenuItem(f.getName() + " (" + path + ")");
				item.addActionListener(e -> openFile(f));
				recentFilesMenu.add(item);
			}
		}
	}

	private void toggleFullScreen() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		mainWindow.dispose();
		if (!isFullScreen) {
			mainWindow.setUndecorated(true);
			gd.setFullScreenWindow(mainWindow);
			isFullScreen = true;
		} else {
			mainWindow.setUndecorated(false);
			gd.setFullScreenWindow(null);
			isFullScreen = false;
		}
		mainWindow.setVisible(true);
	}

	private JFrame createMainWindow() {
		JFrame result = new JFrame();
		result.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		result.setJMenuBar(createMenuBar());

		result.setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			public boolean importData(TransferSupport support) {
				try {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					if (files != null && !files.isEmpty()) {
						File droppedFile = files.get(0);
						if (droppedFile.getName().toLowerCase().endsWith(".epub")) {
							openFile(droppedFile);
							return true;
						}
					}
				} catch (Exception ex) {
					log.error("Failed to process dropped file", ex);
				}
				return false;
			}
		});

		JPanel mainPanel = new JPanel(new BorderLayout());
		
		leftTabbedPane = new JTabbedPane();
		leftTabbedPane.addTab("📖 Contents", new TableOfContentsPane(navigator));
		this.bookmarkPane = new BookmarkPane(navigator);
		leftTabbedPane.addTab("🔖 Bookmarks", bookmarkPane);
		leftTabbedPane.addTab("🗺️ Guide", new GuidePane(navigator));

		rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setContinuousLayout(true);
		rightSplitPane.setResizeWeight(1.0);
		this.htmlPane = new ContentPane(navigator);
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(htmlPane, BorderLayout.CENTER);
		this.browseBar = new BrowseBar(navigator, htmlPane, this);
		contentPanel.add(browseBar, BorderLayout.SOUTH);
		rightSplitPane.setLeftComponent(contentPanel);
		rightSplitPane.setRightComponent(new MetadataPane(navigator));
		
		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.setLeftComponent(leftTabbedPane);
		mainSplitPane.setRightComponent(rightSplitPane);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setContinuousLayout(true);
		mainSplitPane.setResizeWeight(0.0);
		
		mainPanel.add(mainSplitPane, BorderLayout.CENTER);
		mainPanel.setPreferredSize(new Dimension(1100, 780));
		mainPanel.add(new NavigationBar(navigator), BorderLayout.NORTH);

		result.add(mainPanel);
		result.pack();
		setLayout(Layout.TocContentMeta);
		result.setVisible(true);
		return result;
	}
	
	private void gotoBook(Book book) {
		if (book == null) {
			return;
		}
		mainWindow.setTitle(book.getTitle());
		navigator.gotoBook(book, this);
	}

	private static String getText(String text) {
		return text;
	}
	
	private static JFileChooser createFileChooser(File startDir) {
		if (startDir == null) {
			startDir = new File(System.getProperty("user.home"));
			if (! startDir.exists()) {
				startDir = null;
			}
		}
		JFileChooser fileChooser = new JFileChooser(startDir);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(new FileNameExtensionFilter("EPub files", "epub"));
				     
		return fileChooser;
	}
	
	private JMenuBar createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(getText("File"));
		menuBar.add(fileMenu);
		
		JMenuItem openFileMenuItem = new JMenuItem(getText("Open"));
		openFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		var fileHandler = new Object() {
			private File previousDir;
			void openFile() {
				JFileChooser fileChooser = createFileChooser(previousDir);
				int returnVal = fileChooser.showOpenDialog(mainWindow);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile == null) {
					return;
				}
				if (!selectedFile.isDirectory()) {
					previousDir = selectedFile.getParentFile();
				}
				Viewer.this.openFile(selectedFile);
			}
			void saveFile() {
				if (navigator.getBook() == null) {
					return;
				}
				JFileChooser fileChooser = createFileChooser(previousDir);
				int returnVal = fileChooser.showSaveDialog(mainWindow);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile == null) {
					return;
				}
				if (!selectedFile.isDirectory()) {
					previousDir = selectedFile.getParentFile();
				}
				try {
					(new EpubWriter()).write(navigator.getBook(), new FileOutputStream(selectedFile));
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
		};

		openFileMenuItem.addActionListener(e -> fileHandler.openFile());
		fileMenu.add(openFileMenuItem);

		recentFilesMenu = new JMenu(getText("Open Recent"));
		updateRecentFilesMenu();
		fileMenu.add(recentFilesMenu);

		JMenuItem saveFileMenuItem = new JMenuItem(getText("Save as ..."));
		saveFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		saveFileMenuItem.addActionListener(e -> fileHandler.saveFile());
		fileMenu.add(saveFileMenuItem);
		
		JMenuItem reloadMenuItem = new JMenuItem(getText("Reload"));
		reloadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		reloadMenuItem.addActionListener(e -> gotoBook(navigator.getBook()));
		fileMenu.add(reloadMenuItem);

		JMenuItem exitMenuItem = new JMenuItem(getText("Exit"));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		exitMenuItem.addActionListener(e -> System.exit(0));
		fileMenu.add(exitMenuItem);
		
		JMenu viewMenu = new JMenu(getText("View"));
		menuBar.add(viewMenu);
		
		JMenuItem viewTocContentMenuItem = new JMenuItem(getText("TOCContent"), ViewerUtil.createImageIcon("layout-toc-content"));
		viewTocContentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
		viewTocContentMenuItem.addActionListener(e -> setLayout(Layout.TocContent));
		viewMenu.add(viewTocContentMenuItem);

		JMenuItem viewContentMenuItem = new JMenuItem(getText("Content"), ViewerUtil.createImageIcon("layout-content"));
		viewContentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
		viewContentMenuItem.addActionListener(e -> setLayout(Layout.Content));
		viewMenu.add(viewContentMenuItem);

		JMenuItem viewTocContentMetaMenuItem = new JMenuItem(getText("TocContentMeta"), ViewerUtil.createImageIcon("layout-toc-content-meta"));
		viewTocContentMetaMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));
		viewTocContentMetaMenuItem.addActionListener(e -> setLayout(Layout.TocContentMeta));
		viewMenu.add(viewTocContentMetaMenuItem);

		viewMenu.addSeparator();

		JMenuItem fullScreenMenuItem = new JMenuItem(getText("Full Screen (F11)"));
		fullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		fullScreenMenuItem.addActionListener(e -> toggleFullScreen());
		viewMenu.add(fullScreenMenuItem);

		viewMenu.addSeparator();

		JMenuItem zoomInItem = new JMenuItem(getText("Zoom In"));
		zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
		zoomInItem.addActionListener(e -> htmlPane.zoomIn());
		viewMenu.add(zoomInItem);

		JMenuItem zoomOutItem = new JMenuItem(getText("Zoom Out"));
		zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		zoomOutItem.addActionListener(e -> htmlPane.zoomOut());
		viewMenu.add(zoomOutItem);

		JMenuItem resetZoomItem = new JMenuItem(getText("Reset Zoom"));
		resetZoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
		resetZoomItem.addActionListener(e -> htmlPane.resetZoom());
		viewMenu.add(resetZoomItem);

		JMenu fontFamilyMenu = new JMenu(getText("Font Family"));
		ButtonGroup fontGroup = new ButtonGroup();
		for (String font : new String[]{"Sans-Serif", "Serif", "Monospace", "Dialog"}) {
			JRadioButtonMenuItem fontItem = new JRadioButtonMenuItem(font, font.equals("Sans-Serif"));
			fontItem.addActionListener(e -> htmlPane.setFontFamily(font));
			fontGroup.add(fontItem);
			fontFamilyMenu.add(fontItem);
		}
		viewMenu.add(fontFamilyMenu);

		viewMenu.addSeparator();

		JMenu uiThemeMenu = new JMenu(getText("UI Theme (Window)"));
		ButtonGroup uiThemeGroup = new ButtonGroup();
		for (UiTheme theme : UiTheme.values()) {
			JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(theme.getDisplayName(), theme == currentUiTheme);
			themeItem.addActionListener(e -> setUiTheme(theme));
			uiThemeGroup.add(themeItem);
			uiThemeMenu.add(themeItem);
		}
		viewMenu.add(uiThemeMenu);

		JMenu readerThemeMenu = new JMenu(getText("Reader Theme (Page)"));
		ButtonGroup readerThemeGroup = new ButtonGroup();
		readerThemeMenuItems.clear();
		for (ReaderTheme theme : ReaderTheme.values()) {
			JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(theme.getDisplayName(), theme == currentReaderTheme);
			themeItem.addActionListener(e -> setReaderTheme(theme));
			readerThemeGroup.add(themeItem);
			readerThemeMenu.add(themeItem);
			readerThemeMenuItems.put(theme, themeItem);
		}
		viewMenu.add(readerThemeMenu);

		JMenu bookmarkMenu = new JMenu(getText("Bookmarks"));
		menuBar.add(bookmarkMenu);

		JMenuItem addBookmarkMenuItem = new JMenuItem(getText("Add Bookmark"));
		addBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		addBookmarkMenuItem.addActionListener(e -> {
			if (bookmarkPane != null) {
				bookmarkPane.addCurrentBookmark();
			}
		});
		bookmarkMenu.add(addBookmarkMenuItem);

		JMenu helpMenu = new JMenu(getText("Help"));
		menuBar.add(helpMenu);
		JMenuItem aboutMenuItem = new JMenuItem(getText("About"));
		aboutMenuItem.addActionListener(e -> new AboutDialog(Viewer.this.mainWindow));
		helpMenu.add(aboutMenuItem);

		return menuBar;
	}

	private enum Layout {
		TocContentMeta,
		TocContent,
		Content
	}

	private void setLayout(Layout layout) {
		switch (layout) {
			case Content -> {
				mainSplitPane.setDividerLocation(0.0d);
				rightSplitPane.setDividerLocation(1.0d);
			}
			case TocContent -> {
				mainSplitPane.setDividerLocation(0.2d);
				rightSplitPane.setDividerLocation(1.0d);
			}
			case TocContentMeta -> {
				mainSplitPane.setDividerLocation(0.2d);
				rightSplitPane.setDividerLocation(0.6d);
			}
		}
	}
}
