# epublib

[![CI](https://github.com/psiegman/epublib/actions/workflows/ci.yml/badge.svg)](https://github.com/psiegman/epublib/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/nl.siegmann.epublib/epublib-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/nl.siegmann.epublib/epublib-parent)

Epublib is a modern Java library for reading, writing, and manipulating EPUB 2 and EPUB 3 files.

The core library (`epublib-core`) is fully compatible with Android (all API levels below 34 are supported via traditional Java patterns). A command-line interface (`epublib-cli`) and a desktop eBook viewer (`epublib-viewer`) are also provided.

---

## Key Features

* **Fluent Builder API**: Construct valid books programmatically without checked-exception boilerplate.
* **Structural Validation**: Discover metadata and structural spec violations using `EpubValidator` before writing.
* **Memory-Efficient Streaming**: Stream large assets (audio, video, high-res images) directly from streams without materializing them in memory via `EpubStreamReader` and `EpubStreamWriter`.
* **Robust Exception Handling**: Custom unchecked exception hierarchy (`EpublibException` -> `EpubReadException`/`EpubWriteException`) replacing legacy swallowed exceptions.
* **Modern CLI**: Rich command-line interactions powered by PicoCLI.

---

## Creating an EPUB Programmatically

```java
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;

public class CreateEpub {
    public static void main(String[] args) throws Exception {
        // Build the book using the fluent API
        Book book = Book.builder()
                .title("My Modern Book")
                .author(Author.of("Jane", "Doe"))
                .language("en")
                .identifier(Identifier.of(Identifier.Scheme.ISBN, "978-3-16-148410-0", true))
                .coverImage(Resource.fromClasspath("/images/cover.jpg", "cover.jpg"))
                .addSection("Chapter 1", Resource.fromClasspath("/content/chapter1.html", "chapter1.html"))
                .build();

        // Write the EPUB file
        new EpubWriter().write(book, new FileOutputStream("my-book.epub"));
    }
}
```

---

## Memory-Efficient Streaming (Large Books)

For books containing large multimedia resources, use the streaming API to avoid OOM errors:

```java
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubStreamWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class StreamEpub {
    public static void main(String[] args) throws Exception {
        Book book = Book.builder().title("Multimedia Book").build();
        Resource videoPlaceholder = Resource.fromBytes(new byte[0], "media/video.mp4");
        book.getResources().add(videoPlaceholder);

        try (EpubStreamWriter writer = new EpubStreamWriter(new FileOutputStream("multimedia.epub"))) {
            // Write structure first (content.opf, toc.ncx, container.xml)
            writer.writeBookStructure(book);
            
            // Pipe large file content directly from disk
            try (FileInputStream videoStream = new FileInputStream("large-video.mp4")) {
                writer.writeResource(videoPlaceholder, videoStream);
            }
        }
    }
}
```

---

## Command Line Interface (CLI)

Build the complete shaded jar via Maven, then run:

```bash
# Read EPUB metadata and validate it
java -jar epublib-cli.jar read my-book.epub --validate

# Read metadata formatted as JSON
java -jar epublib-cli.jar read my-book.epub --format json

# Validate an EPUB file against the specification
java -jar epublib-cli.jar validate my-book.epub --strict

# Compile a directory of HTML files into a new EPUB
java -jar epublib-cli.jar write ./html-dir --out new-book.epub --title "My Title" --author "Jane Doe"
```

---

## Build Instructions

To build the project and run all unit tests:

```bash
mvn clean verify
```
