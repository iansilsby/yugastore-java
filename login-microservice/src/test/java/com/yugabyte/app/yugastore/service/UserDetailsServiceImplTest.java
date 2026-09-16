package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.yugabyte.app.yugastore.model.Role;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.UserRepository;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  @Test
  void existingUserMapsUsernamePasswordAndAuthorities() {
    User user = user("alice", "encoded");
    user.setRoles(Set.of(role("ROLE_USER"), role("ROLE_ADMIN")));
    when(userRepository.findByUsername("alice")).thenReturn(user);

    UserDetails details = userDetailsService.loadUserByUsername("alice");

    assertThat(details.getUsername()).isEqualTo("alice");
    assertThat(details.getPassword()).isEqualTo("encoded");
    assertThat(details.getAuthorities())
      .extracting(GrantedAuthority::getAuthority)
      .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    assertThat(details.isEnabled()).isTrue();
    assertThat(details.isAccountNonExpired()).isTrue();
    assertThat(details.isAccountNonLocked()).isTrue();
    assertThat(details.isCredentialsNonExpired()).isTrue();
  }

  @Test
  void userWithNoRolesHasNoAuthorities() {
    User user = user("alice", "encoded");
    user.setRoles(Collections.emptySet());
    when(userRepository.findByUsername("alice")).thenReturn(user);

    assertThat(userDetailsService.loadUserByUsername("alice").getAuthorities()).isEmpty();
  }

  @Test
  void unknownUserThrowsWithUsernameAsMessage() {
    when(userRepository.findByUsername("missing")).thenReturn(null);

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessage("missing");
  }

  @Test
  void nullRolesThrowsNullPointerException() {
    User user = user("alice", "encoded");
    when(userRepository.findByUsername("alice")).thenReturn(user);

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("alice"))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void nullPasswordThrowsIllegalArgumentException() {
    User user = user("alice", null);
    user.setRoles(Collections.emptySet());
    when(userRepository.findByUsername("alice")).thenReturn(user);

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("alice"))
      .isInstanceOf(IllegalArgumentException.class);
  }

  private static User user(String username, String password) {
    User user = new User();
    user.setUsername(username);
    user.setPassword(password);
    return user;
  }

  private static Role role(String name) {
    Role role = new Role();
    role.setName(name);
    return role;
  }
}
