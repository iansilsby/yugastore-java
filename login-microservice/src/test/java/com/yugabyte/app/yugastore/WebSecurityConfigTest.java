package com.yugabyte.app.yugastore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

  @Mock
  private UserDetailsService userDetailsService;

  @Test
  void configureGlobalBuildsDatabaseAuthenticationProvider() throws Exception {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    when(userDetailsService.loadUserByUsername("alice"))
      .thenReturn(User.withUsername("alice")
        .password(passwordEncoder.encode("password"))
        .roles("USER")
        .build());
    WebSecurityConfig config = new WebSecurityConfig(userDetailsService, passwordEncoder);
    AuthenticationManagerBuilder builder =
      new AuthenticationManagerBuilder(noOpPostProcessor());

    config.configureGlobal(builder);

    AuthenticationManager authenticationManager = builder.build();
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken("alice", "password"));

    assertThat(authentication.isAuthenticated()).isTrue();
    assertThat(authentication.getAuthorities())
      .extracting(authority -> authority.getAuthority())
      .containsExactly("ROLE_USER");
  }

  @Test
  void configureGlobalRejectsWrongPassword() throws Exception {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    when(userDetailsService.loadUserByUsername("alice"))
      .thenReturn(User.withUsername("alice")
        .password(passwordEncoder.encode("password"))
        .roles("USER")
        .build());
    WebSecurityConfig config = new WebSecurityConfig(userDetailsService, passwordEncoder);
    AuthenticationManagerBuilder builder =
      new AuthenticationManagerBuilder(noOpPostProcessor());
    config.configureGlobal(builder);
    AuthenticationManager authenticationManager = builder.build();

    assertThatThrownBy(() -> authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken("alice", "wrong")))
      .isInstanceOf(BadCredentialsException.class);
  }

  private static ObjectPostProcessor<Object> noOpPostProcessor() {
    return new ObjectPostProcessor<Object>() {
      @Override
      public <O> O postProcess(O object) {
        return object;
      }
    };
  }
}
