package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import nl.siegmann.epublib.util.StringUtil;

/**
 * A Book's identifier.
 * 
 * Defaults to a random UUID and scheme "UUID"
 * 
 * @author paul
 *
 */
public class Identifier implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 955949951416391810L;

	public interface Scheme {
		String UUID = "UUID";
		String ISBN = "ISBN";
		String URL = "URL";
		String URI = "URI";
	}
	
	private boolean bookId = false;
	private String scheme;
	private String value;

	/**
	 * Creates an Identifier with as value a random UUID and scheme "UUID"
	 */
	public Identifier() {
		this(Scheme.UUID, UUID.randomUUID().toString());
	}
	
	
	public Identifier(String scheme, String value) {
		this.scheme = scheme;
		this.value = value;
	}

	/**
	 * Creates an Identifier with the given scheme and value.
	 *
	 * @param scheme the identifier scheme (e.g. {@link Scheme#ISBN}, {@link Scheme#UUID})
	 * @param value  the identifier value
	 * @return a new Identifier instance
	 * @since 5.0
	 */
	public static Identifier of(String scheme, String value) {
		return new Identifier(scheme, value);
	}

	/**
	 * Creates an Identifier with the given scheme, value, and book-id flag.
	 *
	 * @param scheme  the identifier scheme
	 * @param value   the identifier value
	 * @param bookId  whether this identifier is the primary book identifier
	 * @return a new Identifier instance
	 * @since 5.0
	 */
	public static Identifier of(String scheme, String value, boolean bookId) {
		Identifier id = new Identifier(scheme, value);
		id.setBookId(bookId);
		return id;
	}


	/**
	 * The first identifier for which the bookId is true is made the bookId identifier.
	 * If no identifier has bookId == true then the first bookId identifier is written as the primary.
	 * 
	 * @param identifiers
	 * @return The first identifier for which the bookId is true is made the bookId identifier.
	 */
	public static Identifier getBookIdIdentifier(List<Identifier> identifiers) {
		if (identifiers == null || identifiers.isEmpty()) {
			return null;
		}
		return identifiers.stream()
				.filter(Identifier::isBookId)
				.findFirst()
				.orElseGet(() -> identifiers.get(0));
	}
	
	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setBookId(boolean bookId) {
		this.bookId = bookId;
	}

	public boolean isBookId() {
		return bookId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(scheme, value);
	}
	
	@Override
	public boolean equals(Object otherIdentifier) {
		if (this == otherIdentifier) {
			return true;
		}
		if (!(otherIdentifier instanceof Identifier other)) {
			return false;
		}
		return Objects.equals(scheme, other.scheme) && Objects.equals(value, other.value);
	}
	
	@Override
	public String toString() {
		if (StringUtil.isBlank(scheme)) {
			return String.valueOf(value);
		}
		return scheme + ":" + value;
	}
}
