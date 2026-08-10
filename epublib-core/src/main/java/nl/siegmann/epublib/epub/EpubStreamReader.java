package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A memory-efficient streaming reader for EPUB files.
 *
 * <p>Unlike {@link EpubReader}, which materializes all zip entries as byte arrays in memory,
 * {@code EpubStreamReader} parses the structural XML metadata (OPF, NCX, NAV) but reads resources
 * lazily. Caller must call {@link #close()} when done to release the underlying zip file.</p>
 *
 * <pre>
 *     try (EpubStreamReader reader = new EpubStreamReader(new File("large.epub"))) {
 *         Book book = reader.getBook();
 *         // Read resource content dynamically
 *         try (InputStream in = reader.openStream(book.getCoverImage())) {
 *             // process cover
 *         }
 *     }
 * </pre>
 *
 * @since 5.0
 */
public class EpubStreamReader implements AutoCloseable {

    private final ZipFile zipFile;
    private final Book book;

    /**
     * Constructs an EpubStreamReader from the specified EPUB file.
     *
     * @param file the EPUB file to read
     * @throws IOException if an I/O error occurs or the file is not a valid zip
     */
    public EpubStreamReader(File file) throws IOException {
        this.zipFile = new ZipFile(file);
        try {
            this.book = readBookStructure();
        } catch (Exception e) {
            try {
                zipFile.close();
            } catch (IOException ignored) {}
            throw new IOException("Failed to parse EPUB structure: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the parsed {@link Book} structure (metadata, TOC, spine, and manifest resource links).
     *
     * @return the book structure
     */
    public Book getBook() {
        return book;
    }

    /**
     * Opens an {@link InputStream} for the given resource.
     *
     * @param resource the resource to read
     * @return an input stream to read the resource data
     * @throws IOException if the resource is not found or cannot be read
     */
    public InputStream openStream(Resource resource) throws IOException {
        if (resource == null || resource.getHref() == null) {
            throw new IllegalArgumentException("Resource or resource href must not be null");
        }
        ZipEntry entry = zipFile.getEntry(resource.getHref());
        if (entry == null) {
            // try looking up relative to container root if OEBPS etc.
            entry = zipFile.getEntry("OEBPS/" + resource.getHref());
        }
        if (entry == null) {
            throw new IOException("Resource not found in EPUB zip: " + resource.getHref());
        }
        return zipFile.getInputStream(entry);
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    private Book readBookStructure() throws Exception {
        Book book = new Book();
        // 1. Gather resource entries without loading data into memory
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            MediaType mediaType = MediatypeService.determineMediaType(name);
            // Create a lazy resource placeholder without backing byte[] data
            Resource resource = new Resource(null, new byte[0], name, mediaType);
            book.getResources().add(resource);
        }

        // 2. Parse package document to set up metadata and spine
        // Read OPF/container resources directly since they are small and needed for structure
        String packageHref = getPackageResourceHref(book);
        Resource packageResource = book.getResources().getByHref(packageHref);
        if (packageResource == null) {
            packageResource = book.getResources().getByHref("OEBPS/" + packageHref);
        }
        if (packageResource == null) {
            throw new IOException("Container/OPF package resource not found: " + packageHref);
        }

        // Populate the placeholder package resource data so reader can parse it
        try (InputStream in = zipFile.getInputStream(zipFile.getEntry(packageResource.getHref()))) {
            byte[] packageData = IOUtils.toByteArray(in);
            packageResource.setData(packageData);
        }

        PackageDocumentReader.read(packageResource, new EpubReader(), book, book.getResources());
        return book;
    }

    private String getPackageResourceHref(Book book) {
        Resource container = book.getResources().getByHref("META-INF/container.xml");
        if (container == null) {
            return "OEBPS/content.opf";
        }
        try (InputStream in = zipFile.getInputStream(zipFile.getEntry(container.getHref()))) {
            container.setData(IOUtils.toByteArray(in));
            org.w3c.dom.Document doc = nl.siegmann.epublib.util.ResourceUtil.getAsDocument(container);
            org.w3c.dom.Element root = (org.w3c.dom.Element) doc.getDocumentElement().getElementsByTagName("rootfiles").item(0);
            org.w3c.dom.Element rootFile = (org.w3c.dom.Element) root.getElementsByTagName("rootfile").item(0);
            return rootFile.getAttribute("full-path");
        } catch (Exception e) {
            return "OEBPS/content.opf";
        } finally {
            container.setData(new byte[0]); // keep memory usage low
        }
    }
}
