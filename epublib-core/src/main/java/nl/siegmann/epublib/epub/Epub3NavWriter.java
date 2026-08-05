package nl.siegmann.epublib.epub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.service.MediatypeService;

import org.xmlpull.v1.XmlSerializer;

/**
 * Generates an EPUB 3 HTML5 Navigation Document (&lt;nav epub:type="toc"&gt;).
 */
public class Epub3NavWriter {

	public static final String DEFAULT_NAV_HREF = "nav.xhtml";
	public static final String NAV_ITEM_ID = "nav";

	public static Resource createNavResource(Book book) throws IOException {
		return createNavResource(book.getTableOfContents(), book.getTitle());
	}

	public static Resource createNavResource(TableOfContents tableOfContents, String title) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlSerializer serializer = EpubProcessorSupport.createXmlSerializer(out);
		write(serializer, tableOfContents, title);
		return new Resource(NAV_ITEM_ID, out.toByteArray(), DEFAULT_NAV_HREF, MediatypeService.XHTML);
	}

	public static void write(XmlSerializer serializer, TableOfContents tableOfContents, String title) throws IOException {
		serializer.startDocument(Constants.CHARACTER_ENCODING, false);
		serializer.setPrefix("", "http://www.w3.org/1999/xhtml");
		serializer.startTag("http://www.w3.org/1999/xhtml", "html");
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, "xmlns:epub", "http://www.idpf.org/2007/ops");
		
		serializer.startTag("http://www.w3.org/1999/xhtml", "head");
		serializer.startTag("http://www.w3.org/1999/xhtml", "title");
		serializer.text(title != null ? title : "Table of Contents");
		serializer.endTag("http://www.w3.org/1999/xhtml", "title");
		serializer.endTag("http://www.w3.org/1999/xhtml", "head");

		serializer.startTag("http://www.w3.org/1999/xhtml", "body");
		serializer.startTag("http://www.w3.org/1999/xhtml", "nav");
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, "epub:type", "toc");
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, "id", "toc");

		serializer.startTag("http://www.w3.org/1999/xhtml", "h2");
		serializer.text(title != null ? title : "Table of Contents");
		serializer.endTag("http://www.w3.org/1999/xhtml", "h2");

		writeNavOl(serializer, tableOfContents.getTocReferences());

		serializer.endTag("http://www.w3.org/1999/xhtml", "nav");
		serializer.endTag("http://www.w3.org/1999/xhtml", "body");
		serializer.endTag("http://www.w3.org/1999/xhtml", "html");
		serializer.endDocument();
	}

	private static void writeNavOl(XmlSerializer serializer, List<TOCReference> tocReferences) throws IOException {
		if (tocReferences == null || tocReferences.isEmpty()) {
			return;
		}
		serializer.startTag("http://www.w3.org/1999/xhtml", "ol");
		for (TOCReference ref : tocReferences) {
			serializer.startTag("http://www.w3.org/1999/xhtml", "li");
			if (ref.getResource() != null) {
				serializer.startTag("http://www.w3.org/1999/xhtml", "a");
				serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, "href", ref.getCompleteHref());
				serializer.text(ref.getTitle());
				serializer.endTag("http://www.w3.org/1999/xhtml", "a");
			} else {
				serializer.startTag("http://www.w3.org/1999/xhtml", "span");
				serializer.text(ref.getTitle());
				serializer.endTag("http://www.w3.org/1999/xhtml", "span");
			}

			if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
				writeNavOl(serializer, ref.getChildren());
			}
			serializer.endTag("http://www.w3.org/1999/xhtml", "li");
		}
		serializer.endTag("http://www.w3.org/1999/xhtml", "ol");
	}
}
