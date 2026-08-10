package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * MediaType is used to tell the type of content a resource is.
 * 
 * Examples of mediatypes are image/gif, text/css and application/xhtml+xml
 * 
 * All allowed mediaTypes are maintained by the MediaTypeService.
 * 
 * @see nl.siegmann.epublib.service.MediatypeService
 * 
 * @author paul
 *
 */
public class MediaType implements Serializable {

	private static final long serialVersionUID = -7256091153727506788L;
	private final String name;
	private final String defaultExtension;
	private final Collection<String> extensions;
	
	public MediaType(String name, String defaultExtension) {
		this(name, defaultExtension, new String[] {defaultExtension});
	}

	public MediaType(String name, String defaultExtension, String[] extensions) {
		this(name, defaultExtension, Arrays.asList(extensions));
	}
	
	public MediaType(String name, String defaultExtension, Collection<String> extensions) {
		this.name = name;
		this.defaultExtension = defaultExtension;
		this.extensions = extensions;
	}

	public String getName() {
		return name;
	}

	public String getDefaultExtension() {
		return defaultExtension;
	}

	public Collection<String> getExtensions() {
		return extensions;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof MediaType otherMediaType) {
			return Objects.equals(name, otherMediaType.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
