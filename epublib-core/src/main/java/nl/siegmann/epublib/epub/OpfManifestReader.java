package nl.siegmann.epublib.epub;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.service.MediatypeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads the OPF manifest element into Resources.
 */
public class OpfManifestReader extends PackageDocumentBase {

	private static final Logger log = LoggerFactory.getLogger(OpfManifestReader.class);

	public static Resources readManifest(Document packageDocument, String packageHref,
			EpubReader epubReader, Resources resources, Map<String, String> idMapping) {
		Element manifestElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.manifest);
		Resources result = new Resources();
		if (manifestElement == null) {
			log.error("Package document does not contain element {}", OPFTags.manifest);
			return result;
		}
		NodeList itemElements = manifestElement.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.item);
		for (int i = 0; i < itemElements.getLength(); i++) {
			Element itemElement = (Element) itemElements.item(i);
			String id = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.id);
			String href = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.href);
			try {
				href = URLDecoder.decode(href, Constants.CHARACTER_ENCODING);
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
			String mediaTypeName = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.media_type);
			Resource resource = resources.remove(href);
			if (resource == null) {
				log.error("resource with href '{}' not found", href);
				continue;
			}
			resource.setId(id);
			MediaType mediaType = MediatypeService.getMediaTypeByName(mediaTypeName);
			if (mediaType != null) {
				resource.setMediaType(mediaType);
			}
			result.add(resource);
			idMapping.put(id, resource.getId());
		}
		return result;
	}
}
