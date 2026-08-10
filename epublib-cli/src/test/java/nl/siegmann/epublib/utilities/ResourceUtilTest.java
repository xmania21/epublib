package nl.siegmann.epublib.utilities;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.ToolsResourceUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceUtilTest {

	@Test
	public void testFindTitle() {
		String[] testData = new String[] {
				"<html><title>my title1</title><body><h1>heading title1</h1></body></html>", "heading title1",
				"<html><tiTle>my title2</titlE><body><h2>heading title2</h2></body></html>", "heading title2",
				"<html><body><h1>my h1 title3</h1></body></html>", "my h1 title3",
				"<html><head><title>only title tag</title></head><body><p>Text</p></body></html>", "only title tag",
				"<html><body><XH1 class=\"main\">wrong title</Xh1><h2>test title 6</h2></body></html>", "test title 6",
				"<html><body><p>First paragraph first sentence is here. Second sentence.</p></body></html>", "First paragraph first sentence is here"
		};
		for (int i = 0; i < testData.length; i+= 2) {
			Resource resource = new Resource(testData[i].getBytes(), MediatypeService.XHTML);
			String actualTitle = ToolsResourceUtil.findTitleFromXhtml(resource);
			Assertions.assertEquals(testData[i + 1], actualTitle, "Test index " + (i / 2));
		}
	}
}
