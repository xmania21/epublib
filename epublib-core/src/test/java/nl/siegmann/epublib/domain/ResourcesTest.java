package nl.siegmann.epublib.domain;

import nl.siegmann.epublib.service.MediatypeService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourcesTest {
	
	@Test
	public void testGetResourcesByMediaType1() {
		Resources resources = new Resources();
		resources.add(new Resource("foo".getBytes(), MediatypeService.XHTML));
		resources.add(new Resource("bar".getBytes(), MediatypeService.XHTML));
		Assertions.assertEquals(0, resources.getResourcesByMediaType(MediatypeService.PNG).size());
		Assertions.assertEquals(2, resources.getResourcesByMediaType(MediatypeService.XHTML).size());
		Assertions.assertEquals(2, resources.getResourcesByMediaTypes(new MediaType[] {MediatypeService.XHTML}).size());
	}

	@Test
	public void testGetResourcesByMediaType2() {
		Resources resources = new Resources();
		resources.add(new Resource("foo".getBytes(), MediatypeService.XHTML));
		resources.add(new Resource("bar".getBytes(), MediatypeService.PNG));
		resources.add(new Resource("baz".getBytes(), MediatypeService.PNG));
		Assertions.assertEquals(2, resources.getResourcesByMediaType(MediatypeService.PNG).size());
		Assertions.assertEquals(1, resources.getResourcesByMediaType(MediatypeService.XHTML).size());
		Assertions.assertEquals(1, resources.getResourcesByMediaTypes(new MediaType[] {MediatypeService.XHTML}).size());
		Assertions.assertEquals(3, resources.getResourcesByMediaTypes(new MediaType[] {MediatypeService.XHTML, MediatypeService.PNG}).size());
		Assertions.assertEquals(3, resources.getResourcesByMediaTypes(new MediaType[] {MediatypeService.CSS, MediatypeService.XHTML, MediatypeService.PNG}).size());
	}
}
