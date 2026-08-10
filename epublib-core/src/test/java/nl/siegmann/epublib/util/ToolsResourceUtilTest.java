package nl.siegmann.epublib.util;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToolsResourceUtilTest {

	@Test
	public void testFindTitleFromXhtml() {
		// Priority 1: hx tag takes highest priority over <title>
		Resource res1 = new Resource("<html><title>document title</title><body><h1>heading title</h1></body></html>".getBytes(), MediatypeService.XHTML);
		Assertions.assertEquals("heading title", ToolsResourceUtil.findTitleFromXhtml(res1));

		// Priority 2: <title> tag when no hx tag is present
		Resource res2 = new Resource("<html><head><title>my document title</title></head><body><p>Some paragraph text.</p></body></html>".getBytes(), MediatypeService.XHTML);
		Assertions.assertEquals("my document title", ToolsResourceUtil.findTitleFromXhtml(res2));

		// Priority 3: First 50 characters of first sentence in body text when no hx and no title tags exist
		Resource res3 = new Resource("<html><body><p>This is the first sentence of an opening chapter with long text that exceeds fifty characters easily.</p></body></html>".getBytes(), MediatypeService.XHTML);
		Assertions.assertEquals("This is the first sentence of an opening chapter w", ToolsResourceUtil.findTitleFromXhtml(res3));

		// Priority 3 short sentence:
		Resource res4 = new Resource("<html><body><p>Short sentence. Second sentence here.</p></body></html>".getBytes(), MediatypeService.XHTML);
		Assertions.assertEquals("Short sentence", ToolsResourceUtil.findTitleFromXhtml(res4));
	}
}
