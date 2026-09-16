package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  @InjectMocks
  private SecurityServiceImpl securityService;

  @BeforeEach
  void clearContextBefore() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void clearContextAfter() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void findLoggedInUsernameReadsUserDetailsFromAuthenticationDetails() {
    UserDetails userDetails = User.withUsername("alice")
      .password("password")
      .authorities("ROLE_USER")
      .build();
    UsernamePasswordAuthenticationToken token =
      new UsernamePasswordAuthenticationToken("alice", "password");
    token.setDetails(userDetails);
    SecurityContextHolder.getContext().setAuthentication(token);

    assertThat(securityService.findLoggedInUsername()).isEqualTo("alice");
  }

  @Test
  void findLoggedInUsernameReturnsNullForNonUserDetailsDetails() {
    UsernamePasswordAuthenticationToken token =
      new UsernamePasswordAuthenticationToken(
        User.withUsername("alice").password("password").roles("USER").build(),
        "password");
    token.setDetails("details");
    SecurityContextHolder.getContext().setAuthentication(token);

    assertThat(securityService.findLoggedInUsername()).isNull();
  }

  @Test
  void findLoggedInUsernameReturnsNullWhenDetailsAreNull() {
    UsernamePasswordAuthenticationToken token =
      new UsernamePasswordAuthenticationToken(
        User.withUsername("alice").password("password").roles("USER").build(),
        "password");
    SecurityContextHolder.getContext().setAuthentication(token);

    assertThat(securityService.findLoggedInUsername()).isNull();
  }

  @Test
  void findLoggedInUsernameThrowsWithoutAuthentication() {
    assertThatThrownBy(() -> securityService.findLoggedInUsername())
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void autoLoginStoresLocallyConstructedAuthenticatedToken() {
    UserDetails userDetails = User.withUsername("alice")
      .password("encoded")
      .authorities("ROLE_USER")
      .build();
    Authentication differentAuthentication = new UsernamePasswordAuthenticationToken("other", "other");
    when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
    when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
      .thenReturn(differentAuthentication);

    securityService.autoLogin("alice", "password");

    org.mockito.ArgumentCaptor<Authentication> captor =
      org.mockito.ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(captor.capture());
    Authentication token = SecurityContextHolder.getContext().getAuthentication();
    assertThat(token).isSameAs(captor.getValue());
    assertThat(token.getPrincipal()).isSameAs(userDetails);
    assertThat(token.isAuthenticated()).isTrue();
  }

  @Test
  void autoLoginStoresTokenEvenWhenAuthenticateReturnsNull() {
    UserDetails userDetails = User.withUsername("alice")
      .password("encoded")
      .authorities(Collections.emptyList())
      .build();
    when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
    when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(null);

    securityService.autoLogin("alice", "password");

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull()
      .hasFieldOrPropertyWithValue("principal", userDetails);
    assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
  }

  @Test
  void autoLoginPropagatesBadCredentialsAndLeavesContextEmpty() {
    UserDetails userDetails = User.withUsername("alice")
      .password("encoded")
      .authorities("ROLE_USER")
      .build();
    when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
    BadCredentialsException exception = new BadCredentialsException("bad");
    when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenThrow(exception);

    assertThatThrownBy(() -> securityService.autoLogin("alice", "password"))
      .isSameAs(exception);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void autoLoginPropagatesUnknownUserAndDoesNotAuthenticate() {
    UsernameNotFoundException exception = new UsernameNotFoundException("missing");
    when(userDetailsService.loadUserByUsername("missing")).thenThrow(exception);

    assertThatThrownBy(() -> securityService.autoLogin("missing", "password"))
      .isSameAs(exception);
    verify(authenticationManager, never()).authenticate(org.mockito.ArgumentMatchers.any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
