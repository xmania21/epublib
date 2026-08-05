package nl.siegmann.epublib.epub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.ResourceUtil;
import nl.siegmann.epublib.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *  
 * @author paul
 *
 */
public class PackageDocumentReader extends PackageDocumentBase {
	
	private static final Logger log = LoggerFactory.getLogger(PackageDocumentReader.class);
	private static final String[] POSSIBLE_NCX_ITEM_IDS = new String[] {"toc", "ncx", "ncxtoc"};
	
	public static void read(Resource packageResource, EpubReader epubReader, Book book, Resources resources)
			throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
		Document packageDocument = ResourceUtil.getAsDocument(packageResource);
		String packageHref = packageResource.getHref();
		resources = fixHrefs(packageHref, resources);
		OpfGuideReader.readGuide(packageDocument, epubReader, book, resources);
		
		Map<String, String> idMapping = new HashMap<>();
		
		resources = OpfManifestReader.readManifest(packageDocument, packageHref, epubReader, resources, idMapping);
		book.setResources(resources);
		readCover(packageDocument, book);
		book.setMetadata(PackageDocumentMetadataReader.readMetadata(packageDocument));
		book.setSpine(readSpine(packageDocument, book.getResources(), idMapping));
		
		if (book.getCoverPage() == null && book.getSpine().size() > 0) {
			book.setCoverPage(book.getSpine().getResource(0));
		}
	}
	
	/**
	 * Strips off the package prefixes up to the href of the packageHref.
	 * 
	 * @param packageHref
	 * @param resourcesByHref
	 * @return The stripped package href
	 */
	static Resources fixHrefs(String packageHref, Resources resourcesByHref) {
		int lastSlashPos = packageHref.lastIndexOf('/');
		if (lastSlashPos < 0) {
			return resourcesByHref;
		}
		Resources result = new Resources();
		for (Resource resource : resourcesByHref.getAll()) {
			if (StringUtil.isNotBlank(resource.getHref()) && resource.getHref().length() > lastSlashPos) {
				resource.setHref(resource.getHref().substring(lastSlashPos + 1));
			}
			result.add(resource);
		}
		return result;
	}

	/**
	 * Reads the document's spine, containing all sections in reading order.
	 */
	private static Spine readSpine(Document packageDocument, Resources resources, Map<String, String> idMapping) {
		Element spineElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.spine);
		if (spineElement == null) {
			log.error("Element {} not found in package document, generating one automatically", OPFTags.spine);
			return generateSpineFromResources(resources);
		}
		Spine result = new Spine();
		String tocResourceId = DOMUtil.getAttribute(spineElement, NAMESPACE_OPF, OPFAttributes.toc);
		result.setTocResource(findTableOfContentsResource(tocResourceId, resources));
		NodeList spineNodes = packageDocument.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.itemref);
		List<SpineReference> spineReferences = new ArrayList<>(spineNodes.getLength());
		for (int i = 0; i < spineNodes.getLength(); i++) {
			Element spineItem = (Element) spineNodes.item(i);
			String itemref = DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.idref);
			if (StringUtil.isBlank(itemref)) {
				log.error("itemref with missing or empty idref");
				continue;
			}
			String id = idMapping.getOrDefault(itemref, itemref);
			Resource resource = resources.getByIdOrHref(id);
			if (resource == null) {
				log.error("resource with id '{}' not found", id);
				continue;
			}
			
			SpineReference spineReference = new SpineReference(resource);
			if (OPFValues.no.equalsIgnoreCase(DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.linear))) {
				spineReference.setLinear(false);
			}
			spineReferences.add(spineReference);
		}
		result.setSpineReferences(spineReferences);
		return result;
	}

	/**
	 * Creates a spine out of all resources in the resources.
	 */
	private static Spine generateSpineFromResources(Resources resources) {
		Spine result = new Spine();
		List<String> resourceHrefs = new ArrayList<>(resources.getAllHrefs());
		Collections.sort(resourceHrefs, String.CASE_INSENSITIVE_ORDER);
		for (String resourceHref : resourceHrefs) {
			Resource resource = resources.getByHref(resourceHref);
			if (resource.getMediaType() == MediatypeService.NCX) {
				result.setTocResource(resource);
			} else if (resource.getMediaType() == MediatypeService.XHTML) {
				result.addSpineReference(new SpineReference(resource));
			}
		}
		return result;
	}

	/**
	 * Tries several ways of finding the table of contents resource.
	 */
	static Resource findTableOfContentsResource(String tocResourceId, Resources resources) {
		Resource tocResource = null;
		if (StringUtil.isNotBlank(tocResourceId)) {
			tocResource = resources.getByIdOrHref(tocResourceId);
		}
		
		if (tocResource != null) {
			return tocResource;
		}
		
		tocResource = resources.findFirstResourceByMediaType(MediatypeService.NCX);

		if (tocResource == null) {
			for (String possibleId : POSSIBLE_NCX_ITEM_IDS) {
				tocResource = resources.getByIdOrHref(possibleId);
				if (tocResource != null) {
					break;
				}
				tocResource = resources.getByIdOrHref(possibleId.toUpperCase());
				if (tocResource != null) {
					break;
				}
			}
		}

		if (tocResource == null) {
			log.error("Could not find table of contents resource. Tried resource with id '" + tocResourceId + "', "
					+ Constants.DEFAULT_TOC_ID + ", " + Constants.DEFAULT_TOC_ID.toUpperCase() + " and any NCX resource.");
		}
		return tocResource;
	}

	/**
	 * Find all resources that have something to do with the coverpage and the cover image.
	 */
	static Set<String> findCoverHrefs(Document packageDocument) {
		Set<String> result = new HashSet<>();
		
		String coverResourceId = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
				OPFTags.meta, OPFAttributes.name, OPFValues.meta_cover, OPFAttributes.content);

		if (StringUtil.isNotBlank(coverResourceId)) {
			String coverHref = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
					OPFTags.item, OPFAttributes.id, coverResourceId, OPFAttributes.href);
			if (StringUtil.isNotBlank(coverHref)) {
				result.add(coverHref);
			} else {
				result.add(coverResourceId);
			}
		}
		
		String coverHref = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
				OPFTags.reference, OPFAttributes.type, OPFValues.reference_cover, OPFAttributes.href);
		if (StringUtil.isNotBlank(coverHref)) {
			result.add(coverHref);
		}
		return result;
	}

	/**
	 * Finds the cover resource in the packageDocument and adds it to the book if found.
	 */
	private static void readCover(Document packageDocument, Book book) {
		Collection<String> coverHrefs = findCoverHrefs(packageDocument);
		for (String coverHref : coverHrefs) {
			Resource resource = book.getResources().getByHref(coverHref);
			if (resource == null) {
				log.error("Cover resource {} not found", coverHref);
				continue;
			}
			if (resource.getMediaType() == MediatypeService.XHTML) {
				book.setCoverPage(resource);
			} else if (MediatypeService.isBitmapImage(resource.getMediaType())) {
				book.setCoverImage(resource);
			}
		}
	}
}
