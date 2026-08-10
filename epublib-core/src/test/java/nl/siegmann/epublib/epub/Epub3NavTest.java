package nl.siegmann.epublib.epub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TableOfContents;

public class Epub3NavTest {

	@Test
	public void testCreateAndReadEpub3Nav() throws Exception {
		Book book = new Book();
		book.getMetadata().addTitle("EPUB 3 Test Book");
		
		Resource chapter1 = new Resource("chap1", "<html><body><h1>Chapter 1</h1></body></html>".getBytes(), "OEBPS/chapter1.html", nl.siegmann.epublib.service.MediatypeService.XHTML);
		book.addSection("Chapter 1", chapter1);
		
		Resource navResource = Epub3NavWriter.createNavResource(book);
		Assertions.assertNotNull(navResource);
		Assertions.assertEquals("nav", navResource.getId());
		Assertions.assertTrue(new String(navResource.getData()).contains("<nav"));
		
		Book readBook = new Book();
		readBook.getResources().add(chapter1);
		readBook.setTableOfContents(new TableOfContents());
		
		Epub3NavReader.readNav(navResource, new EpubReader(), readBook, readBook.getResources());
		Assertions.assertFalse(readBook.getTableOfContents().isEmpty());
		Assertions.assertEquals("Chapter 1", readBook.getTableOfContents().getTocReferences().get(0).getTitle());
	}
}
