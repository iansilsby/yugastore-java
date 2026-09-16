package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CheckoutStatusTest {

  @Test
  void constants() {
    assertThat(CheckoutStatus.SUCCESS).isEqualTo("SUCCESS");
    assertThat(CheckoutStatus.FAILURE).isEqualTo("FAILURE");
  }

  @Test
  void fieldsDefaultToNull() {
    CheckoutStatus status = new CheckoutStatus();

    assertThat(status.getStatus()).isNull();
    assertThat(status.getOrderNumber()).isNull();
    assertThat(status.getOrderDetails()).isNull();
  }

  @Test
  void gettersAndSetters() {
    CheckoutStatus status = new CheckoutStatus();
    status.setStatus(CheckoutStatus.SUCCESS);
    status.setOrderNumber("ORDER-1");
    status.setOrderDetails("details");

    assertThat(status.getStatus()).isEqualTo("SUCCESS");
    assertThat(status.getOrderNumber()).isEqualTo("ORDER-1");
    assertThat(status.getOrderDetails()).isEqualTo("details");
  }
}
