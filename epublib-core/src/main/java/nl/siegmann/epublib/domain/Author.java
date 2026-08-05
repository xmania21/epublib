package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents one of the authors of the book
 * 
 * @author paul
 *
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
	
	public static Author of(String firstname, String lastname) {
		return new Author(firstname, lastname);
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
