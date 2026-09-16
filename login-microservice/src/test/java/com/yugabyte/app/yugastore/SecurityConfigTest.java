package com.yugabyte.app.yugastore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

  @Test
  void passwordEncoderUsesBcryptAndMatchesEncodedPassword() {
    PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

    String encoded = encoder.encode("secret");

    assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    assertThat(encoded).isNotEqualTo("secret");
    assertThat(encoder.matches("secret", encoded)).isTrue();
  }

  @Test
  void eachPasswordEncoderCallReturnsNewInstance() {
    SecurityConfig config = new SecurityConfig();

    assertThat(config.passwordEncoder()).isNotSameAs(config.passwordEncoder());
  }
}
