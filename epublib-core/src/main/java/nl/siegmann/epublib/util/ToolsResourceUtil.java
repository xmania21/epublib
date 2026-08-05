package nl.siegmann.epublib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubProcessorSupport;
import nl.siegmann.epublib.service.MediatypeService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Various resource utility methods
 * 
 * @author paul
 *
 */
public class ToolsResourceUtil {
	
	private static Logger log = LoggerFactory.getLogger(ToolsResourceUtil.class);

	public static ImageIcon getImageIcon(String resourceHref) {

		try {
			return new ImageIcon(IOUtil.toByteArray(ToolsResourceUtil.class.getResourceAsStream(resourceHref)));
		} catch (IOException e) {
			log.error(e.getMessage());
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
	 * Retrieves whatever it finds between &lt;title&gt;...&lt;/title&gt; or &lt;h1-7&gt;...&lt;/h1-7&gt;.
	 * The first match is returned, even if it is a blank string.
	 * 
	 * @param resource
	 * @return
	 */
	public static String findTitleFromXhtml(Resource resource) {
		String result = null;
		try {
			Reader reader = resource.getReader();
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter(Pattern.compile("<(?i)(title|h[1-6])>"));
			if (scanner.hasNext()) {
				scanner.next();
			}
			if (scanner.hasNext()) {
				Scanner scanner2 = new Scanner(scanner.next());
				scanner2.useDelimiter(Pattern.compile("</(?i)(title|h[1-6])>"));
				if (scanner2.hasNext()) {
					result = scanner2.next();
					result = StringEscapeUtils.unescapeHtml(result);

				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return result;
	}

	public static Resource createResource(File file) throws IOException {
		return createResource(file, Constants.CHARACTER_ENCODING);
	}
	
	public static Resource createResource(File file, String encoding) throws IOException {
		MediaType mediaType = MediatypeService.determineMediaType(file.getName());
		byte[] data = IOUtils.toByteArray(new FileInputStream(file));
		return new Resource(null, data, file.getName(), mediaType, encoding);
	}
	
	public static Resource createResource(String title, String resourceLocation) {
		try {
			return new Resource(null, IOUtil.toByteArray(ToolsResourceUtil.class.getResourceAsStream(resourceLocation)), resourceLocation, MediatypeService.determineMediaType(resourceLocation));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public static String getAsString(String resourceLocation) {
		return getAsString(ToolsResourceUtil.class.getResourceAsStream(resourceLocation));
	}

	public static String getAsString(InputStream inputStream) {

		if (inputStream == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		try (Scanner scanner = new Scanner(inputStream)) {
			while (scanner.hasNextLine()) {
				result.append(scanner.nextLine());
				result.append("\n");
			}
		}
		return result.toString();
	}
}
