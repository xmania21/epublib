package nl.siegmann.epublib.html.jsoup;

import org.junit.Assert;
import org.junit.Test;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;

public class JsoupHtmlProcessorTest {

	@Test
	public void testProcessBook() throws Exception {
		Book book = new Book();
		String unclosedHtml = "<html><head><title>Test</title></head><body><p>Hello world<br>Second line</body></html>";
		Resource resource = new Resource("test", unclosedHtml.getBytes("UTF-8"), "test.html", MediatypeService.XHTML);
		book.addResource(resource);

		JsoupHtmlProcessor processor = new JsoupHtmlProcessor();
		processor.processBook(book);

		String processedHtml = new String(resource.getData(), "UTF-8");
		Assert.assertTrue(processedHtml.contains("<br />") || processedHtml.contains("<br/>"));
		Assert.assertTrue(processedHtml.contains("</p>"));
	}
}
