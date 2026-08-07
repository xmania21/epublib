package nl.siegmann.epublib.utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumberSayerTest {
	@Test
	public void getNumberName_validNumbers_returnsName() {
		Object[] testinput = new Object[] {
			1, "one",
			42, "fourtytwo",
			127, "hundredtwentyseven",
			433, "fourhundredthirtythree"
		};
		for (int i = 0; i < testinput.length; i += 2) {
			Assertions.assertEquals((String) testinput[i + 1], NumberSayer.getNumberName((Integer) testinput[i])); 
		}
	}
}
