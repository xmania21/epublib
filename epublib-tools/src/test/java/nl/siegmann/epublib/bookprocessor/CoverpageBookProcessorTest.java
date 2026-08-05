package nl.siegmann.epublib.bookprocessor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoverpageBookProcessorTest {

	@Test
	public void testCalculateAbsoluteImageHref1() {
		String[] testData = new String[] {
				"/foo/index.html", "bar.html", "/foo/bar.html",
				"/foo/index.html", "../bar.html", "/bar.html",
				"/foo/index.html", "../sub/bar.html", "/sub/bar.html"
		};
		for (int i = 0; i < testData.length; i+= 3) {
			String actualResult = CoverpageBookProcessor.calculateAbsoluteImageHref(testData[i + 1], testData[i]);
			Assertions.assertEquals(testData[i + 2], actualResult);
		}
	}

}
