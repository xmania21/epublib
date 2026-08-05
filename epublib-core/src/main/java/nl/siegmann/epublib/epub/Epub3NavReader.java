package nl.siegmann.epublib.epub;

import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.util.ResourceUtil;
import nl.siegmann.epublib.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads EPUB 3 HTML5 Navigation Documents containing &lt;nav epub:type="toc"&gt;.
 */
public class Epub3NavReader extends PackageDocumentBase {

	private static final Logger log = LoggerFactory.getLogger(Epub3NavReader.class);
	public static final String EPUB_TYPE_TOC = "toc";
	public static final String NAMESPACE_XHTML = "http://www.w3.org/1999/xhtml";

	public static Resource readNav(Resource navResource, EpubReader epubReader, Book book, Resources resources) {
		if (navResource == null) {
			return null;
		}
		try {
			Document navDocument = ResourceUtil.getAsDocument(navResource);
			Element docElement = navDocument.getDocumentElement();
			Element tocNavElement = findTocNavElement(docElement);
			if (tocNavElement == null) {
				return navResource;
			}
			Element olElement = DOMUtil.getFirstElementByTagNameNS(tocNavElement, NAMESPACE_XHTML, "ol");
			if (olElement == null) {
				olElement = DOMUtil.getFirstElementByTagNameNS(tocNavElement, "", "ol");
			}
			if (olElement != null) {
				List<TOCReference> references = readNavOl(olElement, navResource.getHref(), book);
				book.setTableOfContents(new TableOfContents(references));
			}
		} catch (Exception e) {
			log.error("Error reading EPUB 3 NAV document: {}", e.getMessage(), e);
		}
		return navResource;
	}

	private static Element findTocNavElement(Element root) {
		List<Element> navElements = DOMUtil.getElementsByTagNameNS(root, NAMESPACE_XHTML, "nav");
		if (navElements.isEmpty()) {
			navElements = DOMUtil.getElementsByTagNameNS(root, "", "nav");
		}
		for (Element navElement : navElements) {
			String epubType = DOMUtil.getAttribute(navElement, "http://www.idpf.org/2007/ops", "type");
			if (StringUtil.isBlank(epubType)) {
				epubType = navElement.getAttribute("epub:type");
			}
			if (EPUB_TYPE_TOC.equalsIgnoreCase(epubType)) {
				return navElement;
			}
		}
		return navElements.isEmpty() ? null : navElements.get(0);
	}

	private static List<TOCReference> readNavOl(Element olElement, String navHref, Book book) {
		List<TOCReference> result = new ArrayList<>();
		NodeList childNodes = olElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && "li".equalsIgnoreCase(node.getLocalName())) {
				TOCReference ref = readNavLi((Element) node, navHref, book);
				if (ref != null) {
					result.add(ref);
				}
			}
		}
		return result;
	}

	private static TOCReference readNavLi(Element liElement, String navHref, Book book) {
		Element aElement = DOMUtil.getFirstElementByTagNameNS(liElement, NAMESPACE_XHTML, "a");
		if (aElement == null) {
			aElement = DOMUtil.getFirstElementByTagNameNS(liElement, "", "a");
		}
		String title = aElement != null ? DOMUtil.getTextChildrenContent(aElement) : "";
		String hrefAttr = aElement != null ? DOMUtil.getAttribute(aElement, NAMESPACE_XHTML, "href") : "";
		
		String navRoot = StringUtil.substringBeforeLast(navHref, '/');
		if (navRoot.length() == navHref.length()) {
			navRoot = "";
		} else {
			navRoot = navRoot + "/";
		}
		String fullPath = StringUtil.collapsePathDots(navRoot + hrefAttr);
		String resourceHref = StringUtil.substringBefore(fullPath, Constants.FRAGMENT_SEPARATOR_CHAR);
		String fragmentId = StringUtil.substringAfter(fullPath, Constants.FRAGMENT_SEPARATOR_CHAR);
		
		Resource resource = book.getResources().getByHref(resourceHref);
		TOCReference reference = new TOCReference(title, resource, fragmentId);
		
		Element childOl = DOMUtil.getFirstElementByTagNameNS(liElement, NAMESPACE_XHTML, "ol");
		if (childOl == null) {
			childOl = DOMUtil.getFirstElementByTagNameNS(liElement, "", "ol");
		}
		if (childOl != null) {
			reference.setChildren(readNavOl(childOl, navHref, book));
		}
		return reference;
	}
}
