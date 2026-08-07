package nl.siegmann.epublib.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link Resource} static factory methods added in 5.0.
 *
 * @since 5.0
 */
class ResourceFactoryTest {

    @Test
    void fromBytes_createsResourceWithCorrectData() throws Exception {
        byte[] data = "Hello, EPUB!".getBytes();
        Resource resource = Resource.fromBytes(data, "chapter1.html");

        assertThat(resource.getHref()).isEqualTo("chapter1.html");
        assertThat(resource.getData()).isEqualTo(data);
    }

    @Test
    void fromBytes_htmlHref_detectsXhtmlMediaType() throws Exception {
        Resource resource = Resource.fromBytes(new byte[0], "index.xhtml");

        assertThat(resource.getMediaType()).isNotNull();
        assertThat(resource.getMediaType().getName()).isEqualTo("application/xhtml+xml");
    }

    @Test
    void fromBytes_jpgHref_detectsJpegMediaType() throws Exception {
        Resource resource = Resource.fromBytes(new byte[]{1, 2, 3}, "cover.jpg");

        assertThat(resource.getMediaType()).isNotNull();
        assertThat(resource.getMediaType().getName()).isEqualTo("image/jpeg");
    }

    @Test
    void fromStream_readsAllBytesFromStream() throws Exception {
        byte[] data = "Stream data".getBytes();
        java.io.InputStream in = new java.io.ByteArrayInputStream(data);

        Resource resource = Resource.fromStream(in, "data.css");

        assertThat(resource.getData()).isEqualTo(data);
        assertThat(resource.getHref()).isEqualTo("data.css");
    }

    @Test
    void fromClasspath_existingResource_loadsCorrectly() throws Exception {
        // Use a resource that exists on the test classpath
        Resource resource = Resource.fromClasspath("/holmes_scandal_bohemia.html", "holmes.html");

        assertThat(resource).isNotNull();
        assertThat(resource.getHref()).isEqualTo("holmes.html");
        assertThat(resource.getData()).isNotEmpty();
    }

    @Test
    void fromClasspath_nonExistentResource_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Resource.fromClasspath("/does-not-exist.html", "missing.html"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does-not-exist.html");
    }
}
