package nl.siegmann.epublib.fileset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.VFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilesetBookCreatorTest {

	@Test
	public void createBookFromDirectory_singleChapterFile_createsBook() {
		try {
			FileSystemManager fsManager = VFS.getManager();
			FileObject dir = fsManager.resolveFile("ram://test-dir");
			dir.createFolder();
			FileObject chapter1 = dir.resolveFile("chapter1.html", NameScope.CHILD);
			chapter1.createFile();
			IOUtils.copy(this.getClass().getResourceAsStream("/book1/chapter1.html"), chapter1.getContent().getOutputStream());
			Book bookFromDirectory = FilesetBookCreator.createBookFromDirectory(dir, Constants.CHARACTER_ENCODING);
			Assertions.assertEquals(1, bookFromDirectory.getResources().size());
			Assertions.assertEquals(1, bookFromDirectory.getSpine().size());
			Assertions.assertEquals(1, bookFromDirectory.getTableOfContents().size());
		} catch (Exception e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void createBookFromDirectory_validFiles_createsSpineAndToc() {
		try {
			FileObject dir = createDirWithSourceFiles();
			Book book = FilesetBookCreator.createBookFromDirectory(dir);
			Assertions.assertEquals(5, book.getSpine().size());
			Assertions.assertEquals(5, book.getTableOfContents().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void createBookFromDirectory_unsupportedFileExtension_ignoresUnsupportedFiles() {
		try {
			FileObject dir = createDirWithSourceFiles();
			
			// this file should be ignored
			copyInputStreamToFileObject(new ByteArrayInputStream("hi".getBytes()), dir, "foo.nonsense");
			
			Book book = FilesetBookCreator.createBookFromDirectory(dir);
			Assertions.assertEquals(5, book.getSpine().size());
			Assertions.assertEquals(5, book.getTableOfContents().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}

	private FileObject createDirWithSourceFiles() throws IOException {
		FileSystemManager fsManager = VFS.getManager();
		FileObject dir = fsManager.resolveFile("ram://fileset_test_dir");
		dir.createFolder();
		String[] sourceFiles = new String[] {
				"book1.css",
				"chapter1.html",
				"chapter2_1.html",
				"chapter2.html",
				"chapter3.html",
				"cover.html",
				"flowers_320x240.jpg",
				"cover.png"
		};
		String testSourcesDir = "/book1";
		for (String filename: sourceFiles) {
			String sourceFileName = testSourcesDir + "/" + filename;
			copyResourceToFileObject(sourceFileName, dir, filename);
		}
		return dir;
	}
	
	private void copyResourceToFileObject(String resourceUrl, FileObject targetDir, String targetFilename) throws IOException {
		InputStream inputStream = this.getClass().getResourceAsStream(resourceUrl);
		copyInputStreamToFileObject(inputStream, targetDir, targetFilename);
	}
	
	private void copyInputStreamToFileObject(InputStream inputStream, FileObject targetDir, String targetFilename) throws IOException {
		FileObject targetFile = targetDir.resolveFile(targetFilename, NameScope.DESCENDENT);
		targetFile.createFile();
		IOUtils.copy(inputStream, targetFile.getContent().getOutputStream());
		targetFile.getContent().close();
	}
}
