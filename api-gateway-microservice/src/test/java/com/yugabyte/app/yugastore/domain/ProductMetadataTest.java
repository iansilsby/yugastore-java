package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ProductMetadataTest {

  @Test
  void gettersAndSetters() {
    ProductMetadata product = new ProductMetadata();
    Set<String> categories = new HashSet<>(Arrays.asList("books"));
    List<String> list = Collections.singletonList("x");

    product.setId("B001");
    product.setBrand("brand");
    product.setCategories(categories);
    product.setImUrl("http://img");
    product.setPrice(9.99);
    product.setTitle("title");
    product.setDescription("desc");
    product.setAlso_bought(list);
    product.setAlso_viewed(list);
    product.setBought_together(list);
    product.setBuy_after_viewing(list);
    product.setNum_reviews(5);
    product.setNum_stars(4.5);
    product.setAvg_stars(4.2);

    assertThat(product.getId()).isEqualTo("B001");
    assertThat(product.getBrand()).isEqualTo("brand");
    assertThat(product.getCategories()).isSameAs(categories);
    assertThat(product.getImUrl()).isEqualTo("http://img");
    assertThat(product.getPrice()).isEqualTo(9.99);
    assertThat(product.getTitle()).isEqualTo("title");
    assertThat(product.getDescription()).isEqualTo("desc");
    assertThat(product.getAlso_bought()).isSameAs(list);
    assertThat(product.getAlso_viewed()).isSameAs(list);
    assertThat(product.getBought_together()).isSameAs(list);
    assertThat(product.getBuy_after_viewing()).isSameAs(list);
    assertThat(product.getNum_reviews()).isEqualTo(5);
    assertThat(product.getNum_stars()).isEqualTo(4.5);
    assertThat(product.getAvg_stars()).isEqualTo(4.2);
  }

  @Test
  void equalsIsByIdOnly() {
    ProductMetadata a = new ProductMetadata();
    a.setId("B001");
    a.setTitle("one");
    ProductMetadata b = new ProductMetadata();
    b.setId("B001");
    b.setTitle("two");

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
    assertThat(a.equals(a)).isTrue();
  }

  @Test
  void notEqualToNullOrDifferentClassOrDifferentId() {
    ProductMetadata a = new ProductMetadata();
    a.setId("B001");
    ProductMetadata b = new ProductMetadata();
    b.setId("B002");

    assertThat(a.equals(null)).isFalse();
    assertThat(a.equals("B001")).isFalse();
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void equalsAndHashCodeThrowNpeWhenIdIsNull() {
    // Quirk: equals/hashCode dereference id without a null check.
    ProductMetadata a = new ProductMetadata();
    ProductMetadata b = new ProductMetadata();
    b.setId("B001");

    assertThatThrownBy(() -> a.equals(b)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> a.hashCode()).isInstanceOf(NullPointerException.class);
  }
}
