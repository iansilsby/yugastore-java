package com.yugabyte.yugastore.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

	private static final String PREFIX = "Hello, the time at the server is now ";

	private final HelloController controller = new HelloController();

	@Test
	void helloStartsWithPrefixAndEndsWithNewline() {
		String result = controller.hello();
		assertTrue(result.startsWith(PREFIX));
		assertTrue(result.endsWith("\n"));
	}

	@Test
	void twoCallsProduceSamePrefix() {
		String first = controller.hello();
		String second = controller.hello();
		assertTrue(first.startsWith(PREFIX));
		assertTrue(second.startsWith(PREFIX));
		assertEquals(PREFIX.length(), commonPrefixLength(first, second, PREFIX));
	}

	private int commonPrefixLength(String a, String b, String expected) {
		assertTrue(a.startsWith(expected) && b.startsWith(expected));
		return expected.length();
	}
}
