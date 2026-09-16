package com.yugabyte.yugastore.ui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ProductMetadataTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void allFieldsNullByDefault() {
		ProductMetadata m = new ProductMetadata();
		assertNull(m.getBrand());
		assertNull(m.getCategories());
		assertNull(m.getImUrl());
		assertNull(m.getPrice());
		assertNull(m.getSalesRank());
		assertNull(m.getTitle());
		assertNull(m.getRelated());
		assertNull(m.get_links());
		assertNull(m.getNum_reviews());
		assertNull(m.getNum_stars());
		assertNull(m.getAvg_stars());
	}

	@Test
	void brandRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setBrand("brand");
		assertEquals("brand", m.getBrand());
	}

	@Test
	void categoriesRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		List<String> v = List.of("a", "b");
		m.setCategories(v);
		assertEquals(v, m.getCategories());
	}

	@Test
	void imUrlRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setImUrl("http://img");
		assertEquals("http://img", m.getImUrl());
	}

	@Test
	void priceRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setPrice(9.99);
		assertEquals(9.99, m.getPrice());
	}

	@Test
	void salesRankRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		Map<String, Integer> v = Map.of("Books", 1);
		m.setSalesRank(v);
		assertEquals(v, m.getSalesRank());
	}

	@Test
	void titleRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setTitle("title");
		assertEquals("title", m.getTitle());
	}

	@Test
	void relatedRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		Map<String, List<String>> v = Map.of("also_bought", List.of("X"));
		m.setRelated(v);
		assertEquals(v, m.getRelated());
	}

	@Test
	void linksRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		Map<String, Map<String, String>> v = Map.of("self", Map.of("href", "/x"));
		m.set_links(v);
		assertEquals(v, m.get_links());
	}

	@Test
	void numReviewsRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setNum_reviews(42);
		assertEquals(42, m.getNum_reviews());
	}

	@Test
	void numStarsRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setNum_stars(4.5);
		assertEquals(4.5, m.getNum_stars());
	}

	@Test
	void avgStarsRoundtrip() {
		ProductMetadata m = new ProductMetadata();
		m.setAvg_stars(4.2);
		assertEquals(4.2, m.getAvg_stars());
	}

	@Test
	void jacksonDeserializesKnownKeysAndIgnoresUnknown() throws Exception {
		String json = "{"
				+ "\"brand\":\"b\",\"title\":\"t\",\"price\":1.5,"
				+ "\"imUrl\":\"u\",\"categories\":[\"c\"],"
				+ "\"salesRank\":{\"Books\":3},"
				+ "\"related\":{\"also_bought\":[\"X\"]},"
				+ "\"_links\":{\"self\":{\"href\":\"/x\"}},"
				+ "\"num_reviews\":7,\"num_stars\":4.5,\"avg_stars\":4.4,"
				+ "\"foo\":\"unknown\"}";
		ProductMetadata m = mapper.readValue(json, ProductMetadata.class);
		assertEquals("b", m.getBrand());
		assertEquals("t", m.getTitle());
		assertEquals(1.5, m.getPrice());
		assertEquals("u", m.getImUrl());
		assertEquals(List.of("c"), m.getCategories());
		assertEquals(Map.of("Books", 3), m.getSalesRank());
		assertEquals(Map.of("also_bought", List.of("X")), m.getRelated());
		assertEquals(Map.of("self", Map.of("href", "/x")), m.get_links());
		assertEquals(7, m.getNum_reviews());
		assertEquals(4.5, m.getNum_stars());
		assertEquals(4.4, m.getAvg_stars());
	}

	@Test
	void jacksonSerializesWithUnderscorePropertyNames() throws Exception {
		ProductMetadata m = new ProductMetadata();
		m.setNum_reviews(7);
		m.setNum_stars(4.5);
		m.setAvg_stars(4.4);
		m.set_links(Map.of("self", Map.of("href", "/x")));
		m.setImUrl("u");
		String json = mapper.writeValueAsString(m);
		JsonNode node = mapper.readTree(json);
		assertTrue(node.has("num_reviews"));
		assertTrue(node.has("num_stars"));
		assertTrue(node.has("avg_stars"));
		assertTrue(node.has("_links"));
		assertTrue(node.has("imUrl"));
		assertEquals(7, node.get("num_reviews").asInt());
	}
}
