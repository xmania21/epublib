package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Guide;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads the OPF guide element and resolves special references like cover pages.
 */
public class OpfGuideReader extends PackageDocumentBase {

	private static final Logger log = LoggerFactory.getLogger(OpfGuideReader.class);

	public static void readGuide(Document packageDocument, EpubReader epubReader, Book book, Resources resources) {
		Element guideElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.guide);
		if (guideElement == null) {
			return;
		}
		Guide guide = book.getGuide();
		NodeList guideReferences = guideElement.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.reference);
		for (int i = 0; i < guideReferences.getLength(); i++) {
			Element referenceElement = (Element) guideReferences.item(i);
			String resourceHref = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.href);
			if (StringUtil.isBlank(resourceHref)) {
				continue;
			}
			Resource resource = resources.getByHref(StringUtil.substringBefore(resourceHref, Constants.FRAGMENT_SEPARATOR_CHAR));
			if (resource == null) {
				log.error("Guide is referencing resource with href {} which could not be found", resourceHref);
				continue;
			}
			String type = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.type);
			if (StringUtil.isBlank(type)) {
				log.error("Guide is referencing resource with href {} which is missing the 'type' attribute", resourceHref);
				continue;
			}
			if (GuideReference.COVER.equalsIgnoreCase(type)) {
				continue; // cover is handled elsewhere
			}
			String title = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.title);
			String fragmentId = StringUtil.substringAfter(resourceHref, Constants.FRAGMENT_SEPARATOR_CHAR);
			GuideReference reference = new GuideReference(resource, type, title, fragmentId);
			guide.addReference(reference);
		}
	}
}
