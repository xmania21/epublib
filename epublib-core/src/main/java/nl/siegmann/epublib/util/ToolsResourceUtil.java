package nl.siegmann.epublib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.ImageIcon;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various resource utility methods
 * 
 * @author paul
 *
 */
public class ToolsResourceUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ToolsResourceUtil.class);

	public static ImageIcon getImageIcon(String resourceHref) {
		try (InputStream is = ToolsResourceUtil.class.getResourceAsStream(resourceHref)) {
			if (is != null) {
				return new ImageIcon(IOUtil.toByteArray(is));
			}
		} catch (IOException e) {
			log.error("Error loading image icon {}: {}", resourceHref, e.getMessage());
		}
		return null;
	}
	
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
		try (InputStream is = resource.getInputStream()) {
			org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(is, Constants.CHARACTER_ENCODING, "");
			
			// 1. Try first heading tag <h1>-<h6> (hx)
			org.jsoup.nodes.Element heading = doc.selectFirst("h1, h2, h3, h4, h5, h6");
			if (heading != null && !heading.text().isBlank()) {
				return heading.text().trim();
			}
			
			// 2. Fallback to <title> tag if no hx found
			String title = doc.title();
			if (title != null && !title.isBlank()) {
				return title.trim();
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
					return firstSentence;
				}
			}
		} catch (Exception e) {
			log.error("Error parsing XHTML title from resource {}: {}", resource.getHref(), e.getMessage());
		}
		return null;
	}

	public static Resource createResource(File file) throws IOException {
		return createResource(file, Constants.CHARACTER_ENCODING);
	}
	
	public static Resource createResource(File file, String encoding) throws IOException {
		MediaType mediaType = MediatypeService.determineMediaType(file.getName());
		byte[] data;
		try (InputStream fis = new FileInputStream(file)) {
			data = IOUtils.toByteArray(fis);
		}
		return new Resource(null, data, file.getName(), mediaType, encoding);
	}
	
	public static Resource createResource(String title, String resourceLocation) {
		try (InputStream is = ToolsResourceUtil.class.getResourceAsStream(resourceLocation)) {
			if (is != null) {
				return new Resource(null, IOUtil.toByteArray(is), resourceLocation, MediatypeService.determineMediaType(resourceLocation));
			}
		} catch (IOException e) {
			log.error("Error creating resource {}: {}", resourceLocation, e.getMessage());
		}
		return null;
	}

	public static String getAsString(String resourceLocation) {
		try (InputStream is = ToolsResourceUtil.class.getResourceAsStream(resourceLocation)) {
			if (is != null) {
				return getAsString(is);
			}
		} catch (IOException e) {
			log.error("Error reading resource as string {}: {}", resourceLocation, e.getMessage());
		}
		return "";
	}

	public static String getAsString(InputStream inputStream) {
		if (inputStream == null) {
			return "";
		}
		try {
			return IOUtils.toString(inputStream, Constants.CHARACTER_ENCODING);
		} catch (IOException e) {
			log.error("Error converting stream to string: {}", e.getMessage());
			return "";
		}
	}
}
