package com.yugabyte.app.yugastore.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Set;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void defaultValuesAreNull() {
    User user = new User();

    assertThat(user.getId()).isNull();
    assertThat(user.getUsername()).isNull();
    assertThat(user.getPassword()).isNull();
    assertThat(user.getPasswordConfirm()).isNull();
    assertThat(user.getRoles()).isNull();
  }

  @Test
  void gettersAndSettersRoundTripValues() {
    User user = new User();
    Set<Role> roles = Set.of(new Role());

    user.setId(7L);
    user.setUsername("alice");
    user.setPassword("password");
    user.setPasswordConfirm("password");
    user.setRoles(roles);

    assertThat(user.getId()).isEqualTo(7L);
    assertThat(user.getUsername()).isEqualTo("alice");
    assertThat(user.getPassword()).isEqualTo("password");
    assertThat(user.getPasswordConfirm()).isEqualTo("password");
    assertThat(user.getRoles()).isSameAs(roles);
  }

  @Test
  void tableAndTransientAnnotationsMatchCurrentMapping() throws Exception {
    assertThat(User.class.getAnnotation(Table.class).name()).isEqualTo("username");
    Field passwordConfirm = User.class.getDeclaredField("passwordConfirm");
    assertThat(passwordConfirm.isAnnotationPresent(Transient.class)).isTrue();
  }
}
