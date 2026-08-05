package nl.siegmann.epublib.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import nl.siegmann.epublib.browsersupport.NavigationEvent;
import nl.siegmann.epublib.browsersupport.NavigationEventListener;
import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.util.ToolsResourceUtil;

public class BookmarkPane extends JPanel implements NavigationEventListener {

	private static final long serialVersionUID = 1L;
	private final Navigator navigator;
	private final DefaultListModel<BookmarkItem> listModel = new DefaultListModel<>();
	private final JList<BookmarkItem> bookmarkList = new JList<>(listModel);
	private final Preferences prefs = Preferences.userNodeForPackage(BookmarkPane.class);

	public static record BookmarkItem(String href, String title) {
		@Override
		public String toString() {
			return title != null && !title.isBlank() ? title : href;
		}
	}

	public BookmarkPane(Navigator navigator) {
		super(new BorderLayout());
		this.navigator = navigator;
		navigator.addNavigationEventListener(this);

		bookmarkList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof BookmarkItem item) {
					setText("🔖  " + item);
				}
				return this;
			}
		});

		bookmarkList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					BookmarkItem selected = bookmarkList.getSelectedValue();
					if (selected != null && navigator.getBook() != null) {
						Resource resource = navigator.getBook().getResources().getByHref(selected.href());
						if (resource != null) {
							navigator.gotoResource(resource, BookmarkPane.this);
						}
					}
				}
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);

		JButton addButton = new JButton("➕ Add");
		addButton.setToolTipText("Add current section as Bookmark (Ctrl+D)");
		addButton.addActionListener(e -> addCurrentBookmark());

		JButton removeButton = new JButton("🗑️ Remove");
		removeButton.setToolTipText("Remove selected bookmark");
		removeButton.addActionListener(e -> removeSelectedBookmark());

		toolBar.add(addButton);
		toolBar.add(removeButton);

		add(toolBar, BorderLayout.NORTH);
		add(new JScrollPane(bookmarkList), BorderLayout.CENTER);
	}

	public void addCurrentBookmark() {
		if (navigator.getCurrentResource() == null || navigator.getBook() == null) {
			return;
		}
		String href = navigator.getCurrentResource().getHref();
		String rawTitle = ToolsResourceUtil.getTitle(navigator.getCurrentResource());
		String title = sanitizeTitle(rawTitle, href);

		for (int i = 0; i < listModel.getSize(); i++) {
			if (listModel.getElementAt(i).href().equals(href)) {
				return; // Already bookmarked
			}
		}

		BookmarkItem item = new BookmarkItem(href, title);
		listModel.addElement(item);
		saveBookmarks();
	}

	private String sanitizeTitle(String rawTitle, String fallbackHref) {
		if (rawTitle == null || rawTitle.isBlank()) {
			return fallbackHref;
		}
		String clean = rawTitle.replaceAll("<[^>]+>", " ")
				.replaceAll("[\\r\\n\\t]+", " ")
				.replaceAll("\\s+", " ")
				.trim();
		if (clean.isBlank()) {
			return fallbackHref;
		}
		if (clean.length() > 60) {
			return clean.substring(0, 57) + "...";
		}
		return clean;
	}

	public void removeSelectedBookmark() {
		int selectedIndex = bookmarkList.getSelectedIndex();
		if (selectedIndex >= 0) {
			listModel.remove(selectedIndex);
			saveBookmarks();
		}
	}

	private void saveBookmarks() {
		if (navigator.getBook() == null || navigator.getBook().getTitle() == null) {
			return;
		}
		String bookKey = "bm_" + Math.abs(navigator.getBook().getTitle().hashCode());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < listModel.getSize(); i++) {
			BookmarkItem item = listModel.getElementAt(i);
			sb.append(item.href()).append(";;").append(item.title()).append("\n");
		}
		String val = sb.toString();
		if (val.length() > 4000) {
			val = val.substring(0, 4000);
		}
		prefs.put(bookKey, val);
	}

	private void loadBookmarks() {
		listModel.clear();
		if (navigator.getBook() == null || navigator.getBook().getTitle() == null) {
			return;
		}
		String bookKey = "bm_" + Math.abs(navigator.getBook().getTitle().hashCode());
		String saved = prefs.get(bookKey, "");
		if (saved.isBlank()) {
			return;
		}
		for (String line : saved.split("\n")) {
			String[] parts = line.split(";;");
			if (parts.length >= 2) {
				listModel.addElement(new BookmarkItem(parts[0], parts[1]));
			}
		}
	}

	@Override
	public void navigationPerformed(NavigationEvent navigationEvent) {
		if (navigationEvent.isBookChanged()) {
			loadBookmarks();
		}
	}
}
