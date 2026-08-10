package nl.siegmann.epublib.viewer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LibraryBook {
    private String id;
    private String title;
    private String author;
    private String filePath;
    private String coverPath;

    public LibraryBook() {}

    @JsonCreator
    public LibraryBook(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("author") String author,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("coverPath") String coverPath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.coverPath = coverPath;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
}
