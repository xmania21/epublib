package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A memory-efficient streaming writer for EPUB files.
 *
 * <p>Unlike {@link EpubWriter}, which requires all resource byte arrays to be stored in the
 * {@link Book} object before writing, {@code EpubStreamWriter} allows resources to be piped
 * directly from an {@link InputStream} straight to the output zip stream, eliminating memory
 * overhead for large assets (like audio, video, or high-res images).</p>
 *
 * <pre>
 *     try (EpubStreamWriter writer = new EpubStreamWriter(out)) {
 *         writer.writeBookStructure(book); // writes OPF, NCX, metadata
 *         writer.writeResource(myChapterResource, chapterStream);
 *         writer.writeResource(myVideoResource, videoFileStream);
 *     }
 * </pre>
 *
 * @since 5.0
 */
public class EpubStreamWriter implements AutoCloseable {

    private final ZipOutputStream zof;
    private final EpubWriter baseWriter = new EpubWriter();

    /**
     * Constructs an EpubStreamWriter wrapping the target OutputStream.
     *
     * @param out the output stream to write the EPUB zip to
     */
    public EpubStreamWriter(OutputStream out) {
        this.zof = new ZipOutputStream(out);
    }

    /**
     * Writes the required EPUB infrastructure files (mimetype, META-INF/container.xml,
     * and the generated OPF / NCX/ NAV documents) based on the book structure.
     *
     * @param book the book metadata, toc, and manifest references to write
     * @throws IOException if an I/O error occurs
     */
    public void writeBookStructure(Book book) throws IOException {
        // 1. Write the uncompressed mimetype file first
        ZipEntry mimetypeEntry = new ZipEntry("mimetype");
        mimetypeEntry.setMethod(ZipEntry.STORED);
        mimetypeEntry.setSize(20);
        mimetypeEntry.setCrc(0x2cab616f); // CRC32 of "application/epub+zip"
        zof.putNextEntry(mimetypeEntry);
        zof.write("application/epub+zip".getBytes("US-ASCII"));
        zof.closeEntry();

        // 2. Write container metadata
        zof.putNextEntry(new ZipEntry("META-INF/container.xml"));
        Writer containerWriter = new OutputStreamWriter(zof, "UTF-8");
        containerWriter.write("<?xml version=\"1.0\"?>\n");
        containerWriter.write("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n");
        containerWriter.write("\t<rootfiles>\n");
        containerWriter.write("\t\t<rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n");
        containerWriter.write("\t</rootfiles>\n");
        containerWriter.write("</container>");
        containerWriter.flush();
        zof.closeEntry();

        // 3. Write OPF package document
        zof.putNextEntry(new ZipEntry("OEBPS/content.opf"));
        XmlSerializer opfSerializer = EpubProcessorSupport.createXmlSerializer(zof);
        PackageDocumentWriter.write(baseWriter, opfSerializer, book);
        opfSerializer.flush();
        zof.closeEntry();

        // 4. Write NCX table of contents if available
        if (book.getSpine().getTocResource() != null) {
            zof.putNextEntry(new ZipEntry("OEBPS/toc.ncx"));
            XmlSerializer ncxSerializer = EpubProcessorSupport.createXmlSerializer(zof);
            NCXDocument.write(ncxSerializer, book);
            ncxSerializer.flush();
            zof.closeEntry();
        }
    }

    /**
     * Streams the content of a resource into the EPUB package.
     *
     * @param resource the manifest resource containing the destination href
     * @param content  the input stream of the resource content
     * @throws IOException if an I/O error occurs
     */
    public void writeResource(Resource resource, InputStream content) throws IOException {
        if (resource == null || resource.getHref() == null) {
            throw new IllegalArgumentException("Resource or resource href must not be null");
        }
        zof.putNextEntry(new ZipEntry("OEBPS/" + resource.getHref()));
        byte[] buffer = new byte[8192];
        int read;
        while ((read = content.read(buffer)) != -1) {
            zof.write(buffer, 0, read);
        }
        zof.closeEntry();
    }

    @Override
    public void close() throws IOException {
        zof.close();
    }
}
