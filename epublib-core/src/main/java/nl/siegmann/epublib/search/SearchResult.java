package nl.siegmann.epublib.search;

import nl.siegmann.epublib.domain.Resource;

public class SearchResult {
	private int pagePos = -1;
	private String searchTerm;
	private Resource resource;
	private String contextText;
	private int matchIndex;

	public SearchResult(int pagePos, String searchTerm, Resource resource, String contextText, int matchIndex) {
		super();
		this.pagePos = pagePos;
		this.searchTerm = searchTerm;
		this.resource = resource;
		this.contextText = contextText;
		this.matchIndex = matchIndex;
	}
	public int getPagePos() {
		return pagePos;
	}
	public String getSearchTerm() {
		return searchTerm;
	}
	public Resource getResource() {
		return resource;
	}
	public String getContextText() {
		return contextText;
	}
	public int getMatchIndex() {
		return matchIndex;
	}
}