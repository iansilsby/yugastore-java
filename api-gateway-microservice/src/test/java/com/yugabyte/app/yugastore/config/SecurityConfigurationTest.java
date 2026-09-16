package com.yugabyte.app.yugastore.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

class SecurityConfigurationTest {

  @Test
  void isConfigurationExtendingWebSecurityConfigurerAdapter() {
    assertThat(SecurityConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    assertThat(WebSecurityConfigurerAdapter.class.isAssignableFrom(SecurityConfiguration.class)).isTrue();
  }
}
