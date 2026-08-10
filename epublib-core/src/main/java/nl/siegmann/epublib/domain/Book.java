package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a Book.
 * 
 * All resources of a Book (html, css, xml, fonts, images) are represented as Resources. See getResources() for access to these.<br/>
 * A Book has 3 indexes into these Resources, as per the epub specification:
 * <dl>
 * <dt>Spine</dt>
 * <dd>these are the Resources to be shown when a user reads the book from start to finish.</dd>
 * <dt>Table of Contents</dt>
 * <dd>The table of contents references.</dd>
 * <dt>Guide</dt>
 * <dd>Special resources like cover page, glossary, copyright page, etc.</dd>
 * </dl>
 * 
 * @author paul
 *
 */
public class Book implements Serializable {
	
	private static final long serialVersionUID = 2068355170895770100L;

	private Resources resources = new Resources();
	private Metadata metadata = new Metadata();
	private Spine spine = new Spine();
	private TableOfContents tableOfContents = new TableOfContents();
	private Guide guide = new Guide();
	private Resource opfResource;
	private Resource ncxResource;
	private Resource coverImage;
	
	/**
	 * Adds the resource to the table of contents of the book as a child section of the given parentSection
	 * 
	 * @param parentSection
	 * @param sectionTitle
	 * @param resource
	 * @return The table of contents
	 */
	public TOCReference addSection(TOCReference parentSection, String sectionTitle, Resource resource) {
		getResources().add(resource);
		if (spine.findFirstResourceById(resource.getId()) < 0) {
			spine.addSpineReference(new SpineReference(resource));
		}
		return parentSection.addChildSection(new TOCReference(sectionTitle, resource));
	}

	public void generateSpineFromTableOfContents() {
		Spine newSpine = new Spine(tableOfContents);
		
		// in case the tocResource was already found and assigned
		newSpine.setTocResource(this.spine.getTocResource());
		
		this.spine = newSpine;
	}
	
	/**
	 * Adds a resource to the book's set of resources, table of contents and if there is no resource with the id in the spine also adds it to the spine.
	 * 
	 * @param title
	 * @param resource
	 * @return The table of contents
	 */
	public TOCReference addSection(String title, Resource resource) {
		getResources().add(resource);
		TOCReference tocReference = tableOfContents.addTOCReference(new TOCReference(title, resource));
		if (spine.findFirstResourceById(resource.getId()) < 0) {
			spine.addSpineReference(new SpineReference(resource));
		}
		return tocReference;
	}
	
	/**
	 * The Book's metadata (titles, authors, etc)
	 * 
	 * @return The Book's metadata (titles, authors, etc)
	 */
	public Metadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	
	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public Resource addResource(Resource resource) {
		return resources.add(resource);
	}
	
	/**
	 * The collection of all images, chapters, sections, xhtml files, stylesheets, etc that make up the book.
	 * 
	 * @return The collection of all images, chapters, sections, xhtml files, stylesheets, etc that make up the book.
	 */
	public Resources getResources() {
		return resources;
	}

	/**
	 * The sections of the book that should be shown if a user reads the book from start to finish.
	 * 
	 * @return The Spine
	 */
	public Spine getSpine() {
		return spine;
	}

	public void setSpine(Spine spine) {
		this.spine = spine;
	}

	/**
	 * The Table of Contents of the book.
	 * 
	 * @return The Table of Contents of the book.
	 */
	public TableOfContents getTableOfContents() {
		return tableOfContents;
	}

	public void setTableOfContents(TableOfContents tableOfContents) {
		this.tableOfContents = tableOfContents;
	}
	
	/**
	 * The book's cover page as a Resource.
	 * An XHTML document containing a link to the cover image.
	 * 
	 * @return The book's cover page as a Resource
	 */
	public Resource getCoverPage() {
		Resource coverPage = guide.getCoverPage();
		if (coverPage == null && spine.size() > 0) {
			coverPage = spine.getResource(0);
		}
		return coverPage;
	}
	
	public void setCoverPage(Resource coverPage) {
		if (coverPage == null) {
			return;
		}
		if (!resources.containsByHref(coverPage.getHref())) {
			resources.add(coverPage);
		}
		guide.setCoverPage(coverPage);
	}
	
	/**
	 * Gets the first non-blank title from the book's metadata.
	 * 
	 * @return the first non-blank title from the book's metadata.
	 */
	public String getTitle() {
		return getMetadata().getFirstTitle();
	}
	
	/**
	 * The book's cover image.
	 * 
	 * @return The book's cover image.
	 */
	public Resource getCoverImage() {
		return coverImage;
	}

	public void setCoverImage(Resource coverImage) {
		if (coverImage == null) {
			return;
		}
		if (!resources.containsByHref(coverImage.getHref())) {
			resources.add(coverImage);
		}
		this.coverImage = coverImage;
	}
	
	/**
	 * The guide; contains references to special sections of the book like colophon, glossary, etc.
	 * 
	 * @return The guide; contains references to special sections of the book like colophon, glossary, etc.
	 */
	public Guide getGuide() {
		return guide;
	}

	/**
	 * All Resources of the Book that can be reached via the Spine, the TableOfContents or the Guide.
	 * 
	 * @return All Resources of the Book that can be reached via the Spine, the TableOfContents or the Guide.
	 */
	public List<Resource> getContents() {
		Map<String, Resource> result = new LinkedHashMap<>();
		addToContentsResult(getCoverPage(), result);

		for (SpineReference spineReference : getSpine().getSpineReferences()) {
			addToContentsResult(spineReference.getResource(), result);
		}

		for (Resource resource : getTableOfContents().getAllUniqueResources()) {
			addToContentsResult(resource, result);
		}
		
		for (GuideReference guideReference : getGuide().getReferences()) {
			addToContentsResult(guideReference.getResource(), result);
		}

		return new ArrayList<>(result.values());
	}
	
	private static void addToContentsResult(Resource resource, Map<String, Resource> allReachableResources) {
		if (resource != null && !allReachableResources.containsKey(resource.getHref())) {
			allReachableResources.put(resource.getHref(), resource);
		}
	}

	public Resource getOpfResource() {
		return opfResource;
	}
	
	public void setOpfResource(Resource opfResource) {
		this.opfResource = opfResource;
	}
	
	public void setNcxResource(Resource ncxResource) {
		this.ncxResource = ncxResource;
	}

	public Resource getNcxResource() {
		return ncxResource;
	}

	/**
	 * Creates a new {@link Builder} for constructing a {@link Book} with a fluent API.
	 *
	 * <p>Example usage:</p>
	 * <pre>
	 *     Book book = Book.builder()
	 *         .title("My Book")
	 *         .author(new Author("Jane", "Doe"))
	 *         .coverImage(Resource.fromClasspath("/covers/cover.jpg", "cover.jpg"))
	 *         .addSection("Chapter 1", chapter1Resource)
	 *         .build();
	 * </pre>
	 *
	 * @return a new Builder instance
	 * @since 5.0
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Fluent builder for {@link Book}.
	 *
	 * <p>All fields are optional. The underlying {@link Book} mutable API remains
	 * available for post-build modifications.</p>
	 *
	 * @since 5.0
	 */
	public static final class Builder {

		private final Book book = new Book();

		private Builder() {
		}

		/**
		 * Sets the primary title of the book.
		 *
		 * @param title the book title
		 * @return this builder
		 */
		public Builder title(String title) {
			book.getMetadata().addTitle(title);
			return this;
		}

		/**
		 * Adds an author to the book's metadata.
		 *
		 * @param author the author to add
		 * @return this builder
		 */
		public Builder author(Author author) {
			book.getMetadata().addAuthor(author);
			return this;
		}

		/**
		 * Sets the language of the book (IETF BCP 47 tag, e.g. {@code "en"}).
		 *
		 * @param language the language code
		 * @return this builder
		 */
		public Builder language(String language) {
			book.getMetadata().setLanguage(language);
			return this;
		}

		/**
		 * Sets the primary book identifier.
		 *
		 * @param identifier the book identifier
		 * @return this builder
		 */
		public Builder identifier(Identifier identifier) {
			book.getMetadata().addIdentifier(identifier);
			return this;
		}

		/**
		 * Sets the cover image of the book.
		 *
		 * @param coverImage the cover image resource
		 * @return this builder
		 */
		public Builder coverImage(Resource coverImage) {
			book.setCoverImage(coverImage);
			return this;
		}

		/**
		 * Sets the cover page (an XHTML page containing the cover image).
		 *
		 * @param coverPage the cover page resource
		 * @return this builder
		 */
		public Builder coverPage(Resource coverPage) {
			book.setCoverPage(coverPage);
			return this;
		}

		/**
		 * Adds a top-level section to the book's spine and table of contents.
		 *
		 * @param title    the section title
		 * @param resource the section content resource
		 * @return this builder
		 */
		public Builder addSection(String title, Resource resource) {
			book.addSection(title, resource);
			return this;
		}

		/**
		 * Adds a child section under an existing parent section.
		 *
		 * @param parent   the parent TOC reference
		 * @param title    the section title
		 * @param resource the section content resource
		 * @return this builder
		 */
		public Builder addSection(TOCReference parent, String title, Resource resource) {
			book.addSection(parent, title, resource);
			return this;
		}

		/**
		 * Adds a resource (image, CSS, font, etc.) to the book's manifest.
		 *
		 * @param resource the resource to add
		 * @return this builder
		 */
		public Builder addResource(Resource resource) {
			book.addResource(resource);
			return this;
		}

		/**
		 * Adds a publisher to the book's metadata.
		 *
		 * @param publisher the publisher name
		 * @return this builder
		 */
		public Builder publisher(String publisher) {
			book.getMetadata().addPublisher(publisher);
			return this;
		}

		/**
		 * Adds a description to the book's metadata.
		 *
		 * @param description the book description
		 * @return this builder
		 */
		public Builder description(String description) {
			book.getMetadata().addDescription(description);
			return this;
		}

		/**
		 * Builds and returns the configured {@link Book}.
		 *
		 * @return the built Book
		 */
		public Book build() {
			return book;
		}
	}
}
