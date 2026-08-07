package nl.siegmann.epublib.bookprocessor;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoverpageBookProcessorTest {

	@Test
	public void testDoesNotOverwriteExistingCoverPageOrImage() {
		Book book = new Book();
		
		// Add an existing resource with href "cover.html"
		Resource existingCoverPage = new Resource("existing_cover_id", "<html><body>Existing Document</body></html>".getBytes(), "cover.html", MediatypeService.XHTML);
		book.getResources().add(existingCoverPage);

		// Add a cover image without an href set
		Resource coverImage = new Resource(null, new byte[] { 1, 2, 3, 4 }, null, MediatypeService.PNG);
		book.setCoverImage(coverImage);

		CoverpageBookProcessor processor = new CoverpageBookProcessor();
		processor.processBook(book);

		// Existing cover.html must still belong to existingCoverPage
		Assertions.assertEquals(existingCoverPage, book.getResources().getByHref("cover.html"));

		// Newly created cover page must have a unique href like "cover_1.html"
		Assertions.assertNotNull(book.getCoverPage());
		Assertions.assertNotEquals("cover.html", book.getCoverPage().getHref());
		Assertions.assertEquals("cover_1.html", book.getCoverPage().getHref());
	}
}
