package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents one of the authors (or contributors) of the book.
 *
 * <p>An author has a first name, last name, and an optional {@link Relator} role
 * (defaulting to {@link Relator#AUTHOR}). Use the static factory methods for
 * convenient construction:</p>
 * <pre>
 *     Author author = Author.of("Jane", "Doe");
 *     Author editor = Author.of("John", "Smith", Relator.EDITOR);
 * </pre>
 *
 * @author paul
 * @since 5.0 factory methods added
 */
public class Author implements Serializable {
	
	private static final long serialVersionUID = 6663408501416574200L;
	
	private String firstname;
	private String lastname;
	private Relator relator = Relator.AUTHOR;
	
	public Author(String singleName) {
		this("", singleName);
	}
	
	public Author(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	/**
	 * Creates an Author with the given first and last name.
	 *
	 * @param firstname the author's first name
	 * @param lastname  the author's last name
	 * @return a new Author instance
	 * @since 5.0
	 */
	public static Author of(String firstname, String lastname) {
		return new Author(firstname, lastname);
	}

	/**
	 * Creates an Author with the given first name, last name, and relator role.
	 *
	 * @param firstname the author's first name
	 * @param lastname  the author's last name
	 * @param relator   the role of this person in relation to the book
	 * @return a new Author instance
	 * @since 5.0
	 */
	public static Author of(String firstname, String lastname, Relator relator) {
		Author author = new Author(firstname, lastname);
		author.setRelator(relator);
		return author;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	@Override
	public String toString() {
		return lastname + ", " + firstname;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(firstname, lastname);
	}
	
	@Override
	public boolean equals(Object authorObject) {
		if (this == authorObject) {
			return true;
		}
		if (!(authorObject instanceof Author other)) {
			return false;
		}
		return Objects.equals(firstname, other.firstname) && Objects.equals(lastname, other.lastname);
	}

	public Relator setRole(String code) {
		Relator result = Relator.byCode(code);
		if (result == null) {
			result = Relator.AUTHOR;
		}
		this.relator = result;
		return result;
	}

	public Relator getRelator() {
		return relator;
	}

	public void setRelator(Relator relator) {
		this.relator = relator;
	}
}
