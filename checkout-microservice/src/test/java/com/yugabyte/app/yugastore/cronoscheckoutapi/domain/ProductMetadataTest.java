package com.yugabyte.app.yugastore.cronoscheckoutapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ProductMetadataTest {

	private static ProductMetadata product(String id) {
		ProductMetadata product = new ProductMetadata();
		product.setId(id);
		return product;
	}

	@Test
	void settersRoundTrip() {
		ProductMetadata product = product("B00001");
		product.setBrand("Acme");
		product.setCategories(Set.of("Books"));
		product.setImUrl("http://img");
		product.setPrice(4.5);
		product.setTitle("Widget");
		product.setDescription("A widget");
		product.setAlso_bought(List.of("B00002"));
		product.setAlso_viewed(List.of("B00003"));
		product.setBought_together(List.of("B00004"));
		product.setBuy_after_viewing(List.of("B00005"));
		product.setNum_reviews(10);
		product.setNum_stars(45.0);
		product.setAvg_stars(4.5);

		assertEquals("B00001", product.getId());
		assertEquals("Acme", product.getBrand());
		assertEquals(Set.of("Books"), product.getCategories());
		assertEquals("http://img", product.getImUrl());
		assertEquals(4.5, product.getPrice());
		assertEquals("Widget", product.getTitle());
		assertEquals("A widget", product.getDescription());
		assertEquals(List.of("B00002"), product.getAlso_bought());
		assertEquals(List.of("B00003"), product.getAlso_viewed());
		assertEquals(List.of("B00004"), product.getBought_together());
		assertEquals(List.of("B00005"), product.getBuy_after_viewing());
		assertEquals(10, product.getNum_reviews());
		assertEquals(45.0, product.getNum_stars());
		assertEquals(4.5, product.getAvg_stars());
	}

	@Test
	void equalityIsBasedOnIdOnly() {
		ProductMetadata a = product("B00001");
		a.setTitle("Widget");
		ProductMetadata b = product("B00001");
		b.setTitle("Something else");

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(product("B00001"), product("B00002"));
		assertFalse(a.equals(null));
		assertFalse(a.equals("B00001"));
	}

	@Test
	void equalsAndHashCodeFailOnAnUnsetId() {
		ProductMetadata unset = new ProductMetadata();

		assertThrows(NullPointerException.class, unset::hashCode);
		assertThrows(NullPointerException.class, () -> unset.equals(product("B00001")));
	}
}
