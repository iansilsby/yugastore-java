package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductInventoryTest {

  @Test
  void gettersAndSetters() {
    ProductInventory inventory = new ProductInventory();
    inventory.setId("B001");
    inventory.setQuantity(42);

    assertThat(inventory.getId()).isEqualTo("B001");
    assertThat(inventory.getQuantity()).isEqualTo(42);
  }

  @Test
  void equalsIsByIdOnly() {
    ProductInventory a = new ProductInventory();
    a.setId("B001");
    a.setQuantity(1);
    ProductInventory b = new ProductInventory();
    b.setId("B001");
    b.setQuantity(99);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void notEqualToNullOrDifferentClassOrDifferentId() {
    ProductInventory a = new ProductInventory();
    a.setId("B001");
    ProductInventory b = new ProductInventory();
    b.setId("B002");

    assertThat(a.equals(null)).isFalse();
    assertThat(a.equals("B001")).isFalse();
    assertThat(a).isNotEqualTo(b);
    assertThat(a.equals(a)).isTrue();
  }

  @Test
  void equalsAndHashCodeThrowNpeWhenIdIsNull() {
    // Quirk: equals/hashCode dereference id without a null check.
    ProductInventory a = new ProductInventory();
    ProductInventory b = new ProductInventory();
    b.setId("B001");

    assertThatThrownBy(() -> a.equals(b)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> a.hashCode()).isInstanceOf(NullPointerException.class);
  }
}
