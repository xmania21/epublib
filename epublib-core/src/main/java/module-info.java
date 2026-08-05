module nl.siegmann.epublib.core {
	requires transitive java.xml;
	requires transitive org.slf4j;

	exports nl.siegmann.epublib;
	exports nl.siegmann.epublib.browsersupport;
	exports nl.siegmann.epublib.domain;
	exports nl.siegmann.epublib.epub;
	exports nl.siegmann.epublib.service;
	exports nl.siegmann.epublib.util;
	exports nl.siegmann.epublib.util.commons.io;
	exports nl.siegmann.epublib.utilities;
}
