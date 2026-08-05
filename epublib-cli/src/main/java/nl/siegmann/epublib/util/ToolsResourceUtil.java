package nl.siegmann.epublib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various resource utility methods for CLI
 * 
 * @author paul
 *
 */
public class ToolsResourceUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ToolsResourceUtil.class);

	public static String getTitle(Resource resource) {
		if (resource == null) {
			return "";
		}
		if (resource.getMediaType() != MediatypeService.XHTML) {
			return resource.getHref();
		}
		String title = findTitleFromXhtml(resource);
		if (title == null) {
			title = "";
		}
		return title;
	}

	/**
	 * Retrieves heading (hx), title, or first 50 chars of first sentence using JSoup.
	 * 
	 * @param resource XHTML Resource
	 * @return extracted title string, or null if not found
	 */
	public static String findTitleFromXhtml(Resource resource) {
		if (resource == null) {
			return null;
		}
		if (resource.getTitle() != null && !resource.getTitle().isBlank()) {
			return resource.getTitle();
		}
		try (InputStream is = resource.getInputStream()) {
			org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(is, Constants.CHARACTER_ENCODING, "");
			
			// 1. Try first heading tag <h1>-<h6> (hx)
			org.jsoup.nodes.Element heading = doc.selectFirst("h1, h2, h3, h4, h5, h6");
			if (heading != null && !heading.text().isBlank()) {
				String title = heading.text().trim();
				resource.setTitle(title);
				return title;
			}
			
			// 2. Fallback to <title> tag if no hx found
			String title = doc.title();
			if (title != null && !title.isBlank()) {
				title = title.trim();
				resource.setTitle(title);
				return title;
			}
			
			// 3. Fallback to first 50 characters of the first sentence in body
			if (doc.body() != null) {
				String bodyText = doc.body().text().trim();
				if (!bodyText.isEmpty()) {
					int periodIdx = bodyText.indexOf('.');
					String firstSentence = (periodIdx > 0) ? bodyText.substring(0, periodIdx) : bodyText;
					firstSentence = firstSentence.trim();
					if (firstSentence.length() > 50) {
						firstSentence = firstSentence.substring(0, 50).trim();
					}
					resource.setTitle(firstSentence);
					return firstSentence;
				}
			}
		} catch (Exception e) {
			log.error("Error parsing XHTML title from resource {}: {}", resource.getHref(), e.getMessage());
		}
		return null;
	}
}
