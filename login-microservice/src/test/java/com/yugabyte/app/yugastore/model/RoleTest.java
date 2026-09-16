package com.yugabyte.app.yugastore.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void defaultValuesAreNull() {
    Role role = new Role();

    assertThat(role.getId()).isNull();
    assertThat(role.getName()).isNull();
  }

  @Test
  void gettersAndSettersRoundTripValues() {
    Role role = new Role();

    role.setId(7L);
    role.setName("ROLE_USER");

    assertThat(role.getId()).isEqualTo(7L);
    assertThat(role.getName()).isEqualTo("ROLE_USER");
  }
}
