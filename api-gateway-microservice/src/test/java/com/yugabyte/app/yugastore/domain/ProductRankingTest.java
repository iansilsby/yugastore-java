package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductRankingTest {

  private ProductRanking sampleRanking() {
    ProductRankingKey key = new ProductRankingKey();
    key.setId("B001");
    key.setCategory("books");

    ProductRanking ranking = new ProductRanking();
    ranking.setId(key);
    ranking.setSalesRank(3);
    ranking.setTitle("title");
    ranking.setPrice(9.99);
    ranking.setImUrl("http://img");
    ranking.setNum_reviews(5);
    ranking.setNum_stars(4.5);
    ranking.setAvg_stars(4.2);
    return ranking;
  }

  @Test
  void gettersAndSetters() {
    ProductRanking ranking = sampleRanking();

    assertThat(ranking.getId().getAsin()).isEqualTo("B001");
    assertThat(ranking.getId().getCategory()).isEqualTo("books");
    assertThat(ranking.getSalesRank()).isEqualTo(3);
    assertThat(ranking.getTitle()).isEqualTo("title");
    assertThat(ranking.getPrice()).isEqualTo(9.99);
    assertThat(ranking.getImUrl()).isEqualTo("http://img");
    assertThat(ranking.getNum_reviews()).isEqualTo(5);
    assertThat(ranking.getNum_stars()).isEqualTo(4.5);
    assertThat(ranking.getAvg_stars()).isEqualTo(4.2);
  }

  @Test
  void setIdStoresTheProductRankingKey() {
    ProductRanking ranking = new ProductRanking();
    ProductRankingKey key = new ProductRankingKey();
    key.setId("B001");

    ranking.setId(key);

    assertThat(ranking.getId()).isSameAs(key);
  }

  @Test
  void noEqualsOverrideSameDataIsNotEqual() {
    // Quirk: ProductRanking does not override equals/hashCode, so identical
    // contents are still different instances.
    ProductRanking a = sampleRanking();
    ProductRanking b = sampleRanking();

    assertThat(a).isNotEqualTo(b);
    assertThat(a.equals(a)).isTrue();
  }
}
