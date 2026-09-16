package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrderTest {

  @Test
  void gettersAndSetters() {
    Order order = new Order();
    order.setId("o1");
    order.setUser_id(7);
    order.setOrder_details("details");
    order.setOrder_time("2022-01-01");
    order.setOrder_total(9.99);

    assertThat(order.getId()).isEqualTo("o1");
    assertThat(order.getUser_id()).isEqualTo(7);
    assertThat(order.getOrder_details()).isEqualTo("details");
    assertThat(order.getOrder_time()).isEqualTo("2022-01-01");
    assertThat(order.getOrder_total()).isEqualTo(9.99);
  }

  @Test
  void equalsIsByIdOnly() {
    Order a = new Order();
    a.setId("o1");
    a.setOrder_total(10.0);
    Order b = new Order();
    b.setId("o1");
    b.setOrder_total(999.0);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void notEqualToNullOrDifferentClassOrDifferentId() {
    Order a = new Order();
    a.setId("o1");
    Order b = new Order();
    b.setId("o2");

    assertThat(a.equals(null)).isFalse();
    assertThat(a.equals("o1")).isFalse();
    assertThat(a).isNotEqualTo(b);
    assertThat(a.equals(a)).isTrue();
  }

  @Test
  void equalsAndHashCodeThrowNpeWhenIdIsNull() {
    // Quirk: equals/hashCode dereference id without a null check.
    Order a = new Order();
    Order b = new Order();
    b.setId("o1");

    assertThatThrownBy(() -> a.equals(b)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> a.hashCode()).isInstanceOf(NullPointerException.class);
  }
}
