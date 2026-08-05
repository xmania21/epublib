package nl.siegmann.epublib.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectionUtilTest {

	@Test
	public void testIsEmpty_null() {
		Assertions.assertTrue(CollectionUtil.isEmpty(null));
	}

	@Test
	public void testIsEmpty_empty() {
		Assertions.assertTrue(CollectionUtil.isEmpty(new ArrayList<Object>()));
	}

	@Test
	public void testIsEmpty_elements() {
		Assertions.assertFalse(CollectionUtil.isEmpty(Arrays.asList("foo")));
	}
}
