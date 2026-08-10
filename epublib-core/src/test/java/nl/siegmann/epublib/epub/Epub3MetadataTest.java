package nl.siegmann.epublib.epub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Epub3MetadataTest {

	@Test
	public void testEpub3DctermsModifiedWritten() throws Exception {
		Book book = new Book();
		book.getMetadata().addTitle("EPUB 3 Test");
		book.addSection("Section 1", new Resource("<h1>Section 1</h1>".getBytes("UTF-8"), "section1.html"));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		EpubWriter writer = new EpubWriter();
		writer.write(book, out);

		EpubReader reader = new EpubReader();
		Book readBook = reader.readEpub(new ByteArrayInputStream(out.toByteArray()));

		Assertions.assertNotNull(readBook.getMetadata());
		Assertions.assertFalse(readBook.getMetadata().getOtherProperties().isEmpty());

		boolean foundModified = readBook.getMetadata().getOtherProperties().keySet().stream()
				.anyMatch(qName -> "dcterms:modified".equals(qName.getLocalPart()));
		Assertions.assertTrue(foundModified, "Should contain dcterms:modified meta property for EPUB 3 compliance");
	}
}
