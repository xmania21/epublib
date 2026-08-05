package nl.siegmann.epublib.html.jsoup;

import java.io.IOException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.BookProcessor;
import nl.siegmann.epublib.service.MediatypeService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modern HTML5 BookProcessor backed by JSoup for clean XHTML formatting and sanitization.
 */
public class JsoupHtmlProcessor implements BookProcessor {

	private static final Logger log = LoggerFactory.getLogger(JsoupHtmlProcessor.class);

	@Override
	public Book processBook(Book book) {
		for (Resource resource : book.getResources().getAll()) {
			if (resource.getMediaType() == MediatypeService.XHTML) {
				try {
					processResource(resource);
				} catch (Exception e) {
					log.error("Error processing resource " + resource.getHref() + " with JSoup", e);
				}
			}
		}
		return book;
	}

	public void processResource(Resource resource) throws IOException {
		String encoding = resource.getInputEncoding() != null ? resource.getInputEncoding() : "UTF-8";
		String html = new String(resource.getData(), encoding);
		
		Document doc = Jsoup.parse(html, encoding);
		doc.outputSettings()
				.syntax(Document.OutputSettings.Syntax.xml)
				.escapeMode(Entities.EscapeMode.xhtml)
				.charset(encoding);

		resource.setData(doc.html().getBytes(encoding));
	}
}
