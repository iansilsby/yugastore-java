package com.yugabyte.app.yugastore.cronoscheckoutapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

/**
 * {@link ProductRankingKey} has a hand-written equals/hashCode that does not
 * behave like a composite key. These tests document what it actually does.
 */
class ProductRankingKeyTest {

	private static ProductRankingKey key(String asin, String category) {
		ProductRankingKey key = new ProductRankingKey();
		key.setId(asin);
		key.setCategory(category);
		return key;
	}

	@Test
	void setIdPopulatesTheAsin() {
		ProductRankingKey key = key("B00001", "Books");

		assertEquals("B00001", key.getAsin());
		assertEquals("Books", key.getCategory());
		assertTrue(key instanceof Serializable);
	}

	@Test
	void hashCodeUsesTheAsinTwiceAndIgnoresTheCategory() {
		int asinHash = "B00001".hashCode();
		int expected = 31 * (31 * 1 + asinHash) + asinHash;

		assertEquals(expected, key("B00001", "Books").hashCode());
		assertEquals(key("B00001", "Books").hashCode(), key("B00001", "Toys").hashCode());
		assertEquals(31 * 31, key(null, "Books").hashCode());
	}

	@Test
	void keysWithTheSameAsinAndDifferentCategoriesAreEqual() {
		assertTrue(key("B00001", "Books").equals(key("B00001", "Toys")));
	}

	@Test
	void keysWithDifferentAsinsAreAlsoConsideredEqual() {
		assertTrue(key("B00001", "Books").equals(key("B00002", "Books")));
		assertTrue(key("B00001", "Books").equals(key(null, "Books")));
	}

	@Test
	void aNullAsinIsOnlyUnequalToANonNullAsin() {
		assertFalse(key(null, "Books").equals(key("B00001", "Books")));
		assertThrows(NullPointerException.class, () -> key(null, "Books").equals(key(null, "Books")));
	}

	@Test
	void equalsHandlesSelfNullAndForeignTypes() {
		ProductRankingKey key = key("B00001", "Books");

		assertTrue(key.equals(key));
		assertFalse(key.equals(null));
		assertFalse(key.equals("B00001"));
	}
}
