package com.yugabyte.app.yugastore.cronoscheckoutapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ProductRankingTest {

	@Test
	void settersRoundTrip() {
		ProductRankingKey key = new ProductRankingKey();
		key.setId("B00001");
		key.setCategory("Books");

		ProductRanking ranking = new ProductRanking();
		ranking.setId(key);
		ranking.setSalesRank(3);
		ranking.setTitle("Widget");
		ranking.setPrice(4.5);
		ranking.setImUrl("http://img");
		ranking.setNum_reviews(10);
		ranking.setNum_stars(45.0);
		ranking.setAvg_stars(4.5);

		assertSame(key, ranking.getId());
		assertEquals(3, ranking.getSalesRank());
		assertEquals("Widget", ranking.getTitle());
		assertEquals(4.5, ranking.getPrice());
		assertEquals("http://img", ranking.getImUrl());
		assertEquals(10, ranking.getNum_reviews());
		assertEquals(45.0, ranking.getNum_stars());
		assertEquals(4.5, ranking.getAvg_stars());
	}

	@Test
	void defaultsToZeroSalesRankAndNullEverythingElse() {
		ProductRanking ranking = new ProductRanking();

		assertEquals(0, ranking.getSalesRank());
		assertNull(ranking.getId());
		assertNull(ranking.getTitle());
		assertNull(ranking.getPrice());
	}

	@Test
	void usesIdentityEquality() {
		ProductRanking a = new ProductRanking();
		ProductRanking b = new ProductRanking();

		assertNotEquals(a, b);
	}
}
