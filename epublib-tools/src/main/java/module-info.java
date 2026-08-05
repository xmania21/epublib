module nl.siegmann.epublib.tools {
	requires transitive nl.siegmann.epublib.core;
	requires java.desktop;
	requires org.jsoup;
	requires org.htmlcleaner;
	requires com.formdev.flatlaf;
	requires org.slf4j;

	exports nl.siegmann.epublib.bookprocessor;
	exports nl.siegmann.epublib.chm;
	exports nl.siegmann.epublib.fileset;
	exports nl.siegmann.epublib.html.htmlcleaner;
	exports nl.siegmann.epublib.html.jsoup;
	exports nl.siegmann.epublib.search;
	exports nl.siegmann.epublib.viewer;
}
