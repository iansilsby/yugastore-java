package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yugabyte.app.yugastore.model.Role;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.RoleRepository;
import com.yugabyte.app.yugastore.repo.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  void saveEncodesPasswordBeforeSavingUser() {
    User user = new User();
    user.setPassword("raw");
    when(passwordEncoder.encode("raw")).thenReturn("encoded");
    when(roleRepository.findAll()).thenReturn(Collections.emptyList());

    userService.save(user);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    verify(passwordEncoder).encode("raw");
    assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
  }

  @Test
  void saveAssignsEveryRoleAsHashSet() {
    User user = new User();
    Role first = new Role();
    Role second = new Role();
    when(passwordEncoder.encode(any())).thenReturn("encoded");
    when(roleRepository.findAll()).thenReturn(List.of(first, second));

    userService.save(user);

    assertThat(user.getRoles())
      .isInstanceOf(java.util.HashSet.class)
      .containsExactlyInAnyOrder(first, second);
  }

  @Test
  void saveWithNoRolesAssignsEmptySet() {
    User user = new User();
    when(passwordEncoder.encode(any())).thenReturn("encoded");
    when(roleRepository.findAll()).thenReturn(Collections.emptyList());

    userService.save(user);

    assertThat(user.getRoles()).isNotNull().isEmpty();
  }

  @Test
  void saveDelegatesNullPasswordToEncoder() {
    User user = new User();
    when(passwordEncoder.encode(null)).thenReturn("x");
    when(roleRepository.findAll()).thenReturn(Collections.emptyList());

    userService.save(user);

    verify(passwordEncoder).encode(null);
    assertThat(user.getPassword()).isEqualTo("x");
  }

  @Test
  void findByUsernameDelegatesAndReturnsUser() {
    User expected = new User();
    when(userRepository.findByUsername("alice")).thenReturn(expected);

    assertThat(userService.findByUsername("alice")).isSameAs(expected);
    verify(userRepository).findByUsername("alice");
  }

  @Test
  void findByUsernameReturnsNullWhenRepositoryReturnsNull() {
    when(userRepository.findByUsername("missing")).thenReturn(null);

    assertThat(userService.findByUsername("missing")).isNull();
  }
}
