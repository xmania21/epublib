package nl.siegmann.epublib.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.IOUtil;
import nl.siegmann.epublib.util.StringUtil;
import nl.siegmann.epublib.util.commons.io.XmlStreamReader;

/**
 * Represents a resource that is part of the epub.
 * A resource can be a html file, image, xml, etc.
 * 
 * @author paul
 *
 */
public class Resource implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1043946707835004037L;
	private String id;
	private String title;
	private String href;
	protected String originalHref;
	private MediaType mediaType;
	private String inputEncoding = Constants.CHARACTER_ENCODING;
	protected byte[] data;
	
	/**
	 * Creates an empty Resource with the given href.
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
	public Resource(String href) {
		this(null, new byte[0], href, MediatypeService.determineMediaType(href));
	}
	
	/**
	 * Creates a Resource with the given data and MediaType.
	 * The href will be automatically generated.
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param data The Resource's contents
	 * @param mediaType The MediaType of the Resource
	 */
	public Resource(byte[] data, MediaType mediaType) {
		this(null, data, null, mediaType);
	}
	
	/**
	 * Creates a resource with the given data at the specified href.
	 * The MediaType will be determined based on the href extension.
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @see nl.siegmann.epublib.service.MediatypeService#determineMediaType(String)
	 * 
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
	public Resource(byte[] data, String href) {
		this(null, data, href, MediatypeService.determineMediaType(href), Constants.CHARACTER_ENCODING);
	}
	
	/**
	 * Creates a resource with the data from the given Reader at the specified href.
	 * The MediaType will be determined based on the href extension.
	 *
	 * @see nl.siegmann.epublib.service.MediatypeService#determineMediaType(String)
	 *  
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(Reader in, String href) throws IOException {
		this(null, IOUtil.toByteArray(in, Constants.CHARACTER_ENCODING), href, MediatypeService.determineMediaType(href), Constants.CHARACTER_ENCODING);
	}
	
	/**
	 * Creates a resource with the data from the given InputStream at the specified href.
	 * The MediaType will be determined based on the href extension.
	 *
	 * @see nl.siegmann.epublib.service.MediatypeService#determineMediaType(String)
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * It is recommended to us the {@link #Resource(Reader, String)} method for creating textual
	 * (html/css/etc) resources to prevent encoding problems.
	 * Use this method only for binary Resources like images, fonts, etc.
	 * 
	 * 
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(InputStream in, String href) throws IOException {
		this(null, IOUtil.toByteArray(in), href, MediatypeService.determineMediaType(href));
	}
	
	/**
	 * Creates a resource with the given id, data, mediatype at the specified href.
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param id The id of the Resource. Internal use only. Will be auto-generated if it has a null-value.
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 * @param mediaType The resources MediaType
	 */
	public Resource(String id, byte[] data, String href, MediaType mediaType) {
		this(id, data, href, mediaType, Constants.CHARACTER_ENCODING);
	}


	/**
	 * Creates a resource with the given id, data, mediatype at the specified href.
	 * If the data is of a text type (html/css/etc) then it will use the given inputEncoding.
	 * 
	 * @param id The id of the Resource. Internal use only. Will be auto-generated if it has a null-value.
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 * @param mediaType The resources MediaType
	 * @param inputEncoding If the data is of a text type (html/css/etc) then it will use the given inputEncoding.
	 */
	public Resource(String id, byte[] data, String href, MediaType mediaType, String inputEncoding) {
		this.id = id;
		this.href = href;
		this.originalHref = href;
		this.mediaType = mediaType;
		this.inputEncoding = inputEncoding;
		this.data = data;
	}

	// -------------------------------------------------------------------------
	// Static factory methods (5.0+)
	// -------------------------------------------------------------------------

	/**
	 * Creates a Resource from a byte array at the specified href.
	 * The MediaType is determined from the href extension.
	 *
	 * @param data the resource data
	 * @param href the location within the epub (e.g. {@code "chapter1.html"})
	 * @return a new Resource
	 * @since 5.0
	 */
	public static Resource fromBytes(byte[] data, String href) {
		return new Resource(data, href);
	}

	/**
	 * Creates a Resource by reading all bytes from the given InputStream.
	 * The MediaType is determined from the href extension.
	 *
	 * <p>The stream is fully consumed and closed after this call.</p>
	 *
	 * @param in   the input stream to read from
	 * @param href the location within the epub (e.g. {@code "images/cover.jpg"})
	 * @return a new Resource
	 * @throws java.io.IOException if the stream cannot be read
	 * @since 5.0
	 */
	public static Resource fromStream(InputStream in, String href) throws java.io.IOException {
		return new Resource(in, href);
	}

	/**
	 * Creates a Resource by reading a classpath resource.
	 *
	 * <p>Useful in tests and examples:</p>
	 * <pre>
	 *     Resource cover = Resource.fromClasspath("/covers/cover.jpg", "cover.jpg");
	 * </pre>
	 *
	 * @param classpathName the classpath path of the resource (e.g. {@code "/book1/chapter1.html"})
	 * @param href          the location within the epub
	 * @return a new Resource
	 * @throws java.io.IOException          if the classpath resource cannot be read
	 * @throws IllegalArgumentException if the classpath resource is not found
	 * @since 5.0
	 */
	public static Resource fromClasspath(String classpathName, String href) throws java.io.IOException {
		InputStream in = Resource.class.getResourceAsStream(classpathName);
		if (in == null) {
			throw new IllegalArgumentException("Classpath resource not found: " + classpathName);
		}
		return new Resource(in, href);
	}


	/**
	 * Gets the contents of the Resource as an InputStream.
	 *
	 * @return The contents of the Resource.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {

		return new ByteArrayInputStream(getData());
	}
	
	/**
	 * The contents of the resource as a byte[]
	 * 
	 * @return The contents of the resource
	 */
	public byte[] getData() throws IOException {
		return data;
	}

	/**
	 * Tells this resource to release its cached data.
	 * 
	 * If this resource was not lazy-loaded, this is a no-op.
	 */
	public void close() {
	}

	/**
	 * Sets the data of the Resource.
	 * If the data is a of a different type then the original data then make sure to change the MediaType.
	 * 
	 * @param data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Returns the size of this resource in bytes.
	 * 
	 * @return the size.
	 */
	public long getSize() {
		return data.length;
	}
	
	/**
	 * If the title is found by scanning the underlying html document then it is cached here.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the Resource's id: Make sure it is unique and a valid identifier.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * The resources Id.
	 * 
	 * Must be both unique within all the resources of this book and a valid identifier.
	 * @return The resources Id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The location of the resource within the contents folder of the epub file.
	 * 
	 * Example:<br/>
	 * images/cover.jpg<br/>
	 * content/chapter1.xhtml<br/>
	 * 
	 * @return The location of the resource within the contents folder of the epub file.
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Sets the Resource's href.
	 * 
	 * @param href
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * The character encoding of the resource.
	 * Is allowed to be null for non-text resources like images.
	 * 
	 * @return The character encoding of the resource.
	 */
	public String getInputEncoding() {
		return inputEncoding;
	}
	
	/**
	 * Sets the Resource's input character encoding.
	 * 
	 * @param encoding
	 */
	public void setInputEncoding(String encoding) {
		this.inputEncoding = encoding;
	}
	
	/**
	 * Gets the contents of the Resource as Reader.
	 * 
	 * Does all sorts of smart things (courtesy of apache commons io XMLStreamREader) to handle encodings, byte order markers, etc.
	 * 
	 * @return the contents of the Resource as Reader.
	 * @throws IOException
	 */
	public Reader getReader() throws IOException {
		return new XmlStreamReader(new ByteArrayInputStream(getData()), getInputEncoding());
	}
	
	/**
	 * Gets the hashCode of the Resource's href.
	 * 
	 */
	public int hashCode() {
		return href.hashCode();
	}
	
	/**
	 * Checks to see of the given resourceObject is a resource and whether its href is equal to this one.
	 * 
	 * @return whether the given resourceObject is a resource and whether its href is equal to this one.
	 */
	public boolean equals(Object resourceObject) {
		if (! (resourceObject instanceof Resource)) {
			return false;
		}
		return href.equals(((Resource) resourceObject).getHref());
	}
	
	/**
	 * This resource's mediaType.
	 * 
	 * @return This resource's mediaType.
	 */
	public MediaType getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		return StringUtil.toString("id", id,
				"title", title,
				"encoding", inputEncoding,
				"mediaType", mediaType,
				"href", href,
				"size", (data == null ? 0 : data.length));
	}
}
