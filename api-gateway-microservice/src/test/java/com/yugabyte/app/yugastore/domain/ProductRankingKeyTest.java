package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductRankingKeyTest {

  private ProductRankingKey key(String asin, String category) {
    ProductRankingKey key = new ProductRankingKey();
    key.setId(asin);
    key.setCategory(category);
    return key;
  }

  @Test
  void setIdSetsAsinAndGettersWork() {
    // Quirk: the setter is named setId, not setAsin, but it writes the asin field.
    ProductRankingKey key = key("B001", "books");

    assertThat(key.getAsin()).isEqualTo("B001");
    assertThat(key.getCategory()).isEqualTo("books");
  }

  @Test
  void hashCodeIgnoresCategory() {
    // Quirk: hashCode never mixes in category and applies asin.hashCode() twice.
    ProductRankingKey a = key("B001", "books");
    ProductRankingKey b = key("B001", "movies");

    assertThat(a.hashCode()).isEqualTo(b.hashCode());
    assertThat(a.hashCode()).isEqualTo(31 * (31 + "B001".hashCode()) + "B001".hashCode());
  }

  @Test
  void equalsIgnoresCategoryAndOtherAsinWhenThisAsinIsNotNull() {
    // Quirk: when this.asin != null, equals returns true for any other key.
    assertThat(key("A", "c1").equals(key("B", "c2"))).isTrue();
    assertThat(key("A", "c1").equals(key("A", "c2"))).isTrue();
    assertThat(key("A", "c1").equals(key(null, null))).isTrue();
  }

  @Test
  void equalsWithNullAsin() {
    // Quirk: when this.asin == null and other.asin != null the result is false;
    // when both are null the code dereferences the null asin and throws NPE.
    assertThat(key(null, "c1").equals(key("A", "c1"))).isFalse();
    assertThatThrownBy(() -> key(null, null).equals(key(null, null)))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void equalsEdgeCases() {
    ProductRankingKey key = key("A", "c1");

    assertThat(key.equals(key)).isTrue();
    assertThat(key.equals(null)).isFalse();
    assertThat(key.equals("A")).isFalse();
  }
}
