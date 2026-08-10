package nl.siegmann.epublib.search;

import java.io.IOException;
import java.io.Reader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A searchindex for searching through a book.
 * 
 * @author paul.siegmann
 *
 */
public class SearchIndex {

	private static final Logger log = LoggerFactory.getLogger(SearchIndex.class);
	
	public static int NBSP = 0x00A0;

	// whitespace pattern that also matches U+00A0 (&nbsp; in html)
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\p{Z}\\s]+");
	
	private static final Pattern REMOVE_ACCENT_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); 
	
	private List<ResourceSearchIndex> resourceSearchIndexes = new ArrayList<ResourceSearchIndex>();
	private Book book;
	
	public SearchIndex() {
	}
	
	public SearchIndex(Book book) {
		initBook(book);
	}
	
	public Book getBook() {
		return book;
	}

	
	private static class ResourceSearchIndex {
		private String content;
		private Resource resource;

		public String getContent() {
			return content;
		}

		public Resource getResource() {
			return resource;
		}

		public ResourceSearchIndex(Resource resource, String searchContent) {
			this.resource = resource;
			this.content = searchContent;
		}
	}

	private static ResourceSearchIndex createResourceSearchIndex(Resource resource) {
		String searchContent = getSearchContent(resource);
		if ( StringUtils.isBlank(searchContent)) {
			return null;
		}
		ResourceSearchIndex searchIndex = new ResourceSearchIndex(resource, searchContent);
		return searchIndex;
	}
	
	public void initBook(Book book) {
		this.resourceSearchIndexes = createSearchIndex(book);
	}
	
	private static List<ResourceSearchIndex> createSearchIndex(Book book) {
		List<ResourceSearchIndex> result = new ArrayList<ResourceSearchIndex>();
		if (book == null) {
			return result;
		}
		for (Resource resource: book.getContents()) {
			ResourceSearchIndex resourceSearchIndex = createResourceSearchIndex(resource);
			if (resourceSearchIndex != null) {
				result.add(resourceSearchIndex);
			}
		}
		return result;
	}
	
	public SearchResults doSearch(String searchTerm) {
		SearchResults result = new SearchResults();
		if (StringUtils.isBlank(searchTerm)) {
			return result;
		}
		searchTerm = cleanText(searchTerm);
		for (ResourceSearchIndex resourceSearchIndex: resourceSearchIndexes) {
			result.addAll(doSearch(searchTerm, resourceSearchIndex));
		}
		result.setSearchTerm(searchTerm);
		return result;
	}
	

	public static String getSearchContent(Resource resource) {
		if (resource.getMediaType() != MediatypeService.XHTML) {
			return "";
		}
		try {
			String html = new String(resource.getData(), resource.getInputEncoding());
			org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
			return cleanText(doc.text());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return "";
		}
	}

	public static String getSearchContent(Reader content) {
		if (content == null) {
			return "";
		}
		try (Reader r = content) {
			String html = org.apache.commons.io.IOUtils.toString(r);
			org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
			return cleanText(doc.text());
		} catch (IOException e) {
			log.error("Error reading search content: {}", e.getMessage(), e);
			return "";
		}
	}
	
	/**
	 * Checks whether the given character is a java whitespace or a non-breaking-space (&amp;nbsp;).
	 * 
	 * @param c
	 * @return whether the given character is a java whitespace or a non-breaking-space (&amp;nbsp;).
	 */
	private static boolean isHtmlWhitespace(int c) {
		return c == NBSP || Character.isWhitespace(c);
	}
	
	public static String unicodeTrim(String text) {
		int leadingWhitespaceCount = 0;
		int trailingWhitespaceCount = 0;
		for (int i = 0; i < text.length(); i++) {
			if (! isHtmlWhitespace(text.charAt(i))) {
				break;
			}
			leadingWhitespaceCount++;
		}
		for (int i = (text.length() - 1); i > leadingWhitespaceCount; i--) {
			if (! isHtmlWhitespace(text.charAt(i))) {
				break;
			}
			trailingWhitespaceCount++;
		}
		if (leadingWhitespaceCount > 0 || trailingWhitespaceCount > 0) {
			text = text.substring(leadingWhitespaceCount, text.length() - trailingWhitespaceCount);
		}
		return text;
	}

	/**
	 * Turns html encoded text into plain text.
	 * 
	 * Replaces &amp;ouml; type of expressions into &uml;<br/>
	 * Removes accents<br/>
	 * Replaces multiple whitespaces with a single space.<br/>
	 * 
	 * @param text
	 * @return html encoded text turned into plain text.
	 */
	public static String cleanText(String text) {
		text = unicodeTrim(text);
		
		// replace all multiple whitespaces by a single space
		Matcher matcher = WHITESPACE_PATTERN.matcher(text);
	    text = matcher.replaceAll(" ");

		// turn accented characters into normalized form. Turns &ouml; into o"
		text = Normalizer.normalize(text, Normalizer.Form.NFD);  
		
		// removes the marks found in the previous line.
		text = REMOVE_ACCENT_PATTERN.matcher(text).replaceAll("");
		
		// lowercase everything
		text = text.toLowerCase();
		return text;
	}

	
	private static List<SearchResult> doSearch(String searchTerm, ResourceSearchIndex resourceSearchIndex) {
		return doSearch(searchTerm, resourceSearchIndex.getContent(), resourceSearchIndex.getResource());
	}
	
	protected static List<SearchResult> doSearch(String searchTerm, String content, Resource resource) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		int findPos = content.indexOf(searchTerm);
		int matchIndex = 0;
		while(findPos >= 0) {
			// Extract context text (about 40 chars before and after)
			int start = Math.max(0, findPos - 40);
			int end = Math.min(content.length(), findPos + searchTerm.length() + 40);
			String contextText = content.substring(start, end);
			
			// Clean up context to avoid broken words
			if (start > 0) {
				int firstSpace = contextText.indexOf(' ');
				if (firstSpace != -1 && firstSpace < 20) {
					contextText = "..." + contextText.substring(firstSpace + 1);
				} else {
					contextText = "..." + contextText;
				}
			}
			if (end < content.length()) {
				int lastSpace = contextText.lastIndexOf(' ');
				if (lastSpace != -1 && lastSpace > contextText.length() - 20) {
					contextText = contextText.substring(0, lastSpace) + "...";
				} else {
					contextText = contextText + "...";
				}
			}

			SearchResult searchResult = new SearchResult(findPos, searchTerm, resource, contextText.trim(), matchIndex);
			result.add(searchResult);
			findPos = content.indexOf(searchTerm, findPos + 1);
			matchIndex++;
		}
		return result;
	}
}
