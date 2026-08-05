package nl.siegmann.epublib.epub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import nl.siegmann.epublib.domain.LazyResource;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.CollectionUtil;
import nl.siegmann.epublib.util.ResourceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads Resources from inputStreams, ZipFiles, etc
 * 
 * @author paul
 *
 */
public class ResourcesLoader {

	private static final Logger LOG = LoggerFactory.getLogger(ResourcesLoader.class);

	/**
	 * Loads the entries of the zipFile as resources.
	 * 
	 * The MediaTypes that are in the lazyLoadedTypes will not get their contents loaded, but are stored as references to
	 * entries into the ZipFile and are loaded on demand by the Resource system.
	 * 
	 * @param zipFile
	 * @param defaultHtmlEncoding
	 * @param lazyLoadedTypes
	 * @return Resources object containing all loaded resources
	 * @throws IOException
	 */
	public static Resources loadResources(ZipFile zipFile, String defaultHtmlEncoding,
			List<MediaType> lazyLoadedTypes) throws IOException {		
				
		Resources result = new Resources();
		List<? extends ZipEntry> entryList = Collections.list(zipFile.entries());

		try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
			List<java.util.concurrent.Future<Resource>> futures = new ArrayList<>();

			for (ZipEntry zipEntry : entryList) {
				if (zipEntry == null || zipEntry.isDirectory()) {
					continue;
				}
				String href = zipEntry.getName();
				futures.add(executor.submit(() -> {
					Resource resource;
					if (shouldLoadLazy(href, lazyLoadedTypes)) {
						resource = new LazyResource(zipFile.getName(), zipEntry.getSize(), href);
					} else {
						try (var in = zipFile.getInputStream(zipEntry)) {
							resource = ResourceUtil.createResource(zipEntry, in);
						}
					}
					if (resource.getMediaType() == MediatypeService.XHTML) {
						resource.setInputEncoding(defaultHtmlEncoding);
					}
					return resource;
				}));
			}

			for (var future : futures) {
				try {
					result.add(future.get());
				} catch (Exception e) {
					LOG.error("Error reading zip entry concurrently", e);
				}
			}
		}

		return result;
	}
	
	/**
	 * Whether the given href will load a mediaType that is in the collection of lazilyLoadedMediaTypes.
	 */
	private static boolean shouldLoadLazy(String href, Collection<MediaType> lazilyLoadedMediaTypes) {
		if (CollectionUtil.isEmpty(lazilyLoadedMediaTypes)) {
			return false;
		}
		MediaType mediaType = MediatypeService.determineMediaType(href);
		return lazilyLoadedMediaTypes.contains(mediaType);
	}

	/**
	 * Loads all entries from the ZipInputStream as Resources.
	 * 
	 * @param zipInputStream
	 * @param defaultHtmlEncoding
	 * @return Resources
	 * @throws IOException
	 */
	public static Resources loadResources(ZipInputStream zipInputStream, String defaultHtmlEncoding) throws IOException {
		Resources result = new Resources();
		ZipEntry zipEntry;
		do {
			zipEntry = getNextZipEntry(zipInputStream);
			if (zipEntry == null || zipEntry.isDirectory()) {
				continue;
			}
			
			Resource resource = ResourceUtil.createResource(zipEntry, zipInputStream);
			if (resource.getMediaType() == MediatypeService.XHTML) {
				resource.setInputEncoding(defaultHtmlEncoding);
			}
			result.add(resource);
		} while (zipEntry != null);

		return result;
	}

	private static ZipEntry getNextZipEntry(ZipInputStream zipInputStream) throws IOException {
		try {
			return zipInputStream.getNextEntry();
		} catch (ZipException e) {
			LOG.error("Invalid or damaged zip file.", e);
			try {
				zipInputStream.closeEntry();
			} catch (Exception ignored) {
			}
			throw e;
		}
	}

	/**
	 * Loads all entries from the ZipFile as Resources.
	 * 
	 * @param zipFile
	 * @param defaultHtmlEncoding
	 * @return Resources
	 * @throws IOException
	 */
	public static Resources loadResources(ZipFile zipFile, String defaultHtmlEncoding) throws IOException {
		return loadResources(zipFile, defaultHtmlEncoding, Collections.emptyList());
	}
}
