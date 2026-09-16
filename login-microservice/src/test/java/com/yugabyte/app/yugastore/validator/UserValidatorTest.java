package com.yugabyte.app.yugastore.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

  @Mock
  private UserService userService;

  @InjectMocks
  private UserValidator validator;

  @Test
  void supportsOnlyExactlyUserClass() {
    assertThat(validator.supports(User.class)).isTrue();
    assertThat(validator.supports(Object.class)).isFalse();
    assertThat(validator.supports(SpecialUser.class)).isFalse();
  }

  @Test
  void validUserHasNoErrors() {
    User user = validUser();
    when(userService.findByUsername("alice1")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void usernameFiveCharactersHasSizeError() {
    User user = validUser();
    user.setUsername("alice");
    when(userService.findByUsername("alice")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldError("username").getCode()).isEqualTo("Size.userForm.username");
  }

  @Test
  void usernameThirtyThreeCharactersHasSizeError() {
    User user = validUser();
    user.setUsername("a".repeat(33));
    when(userService.findByUsername(user.getUsername())).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldErrors("username"))
      .extracting(error -> error.getCode())
      .contains("Size.userForm.username");
  }

  @Test
  void usernameBoundaryLengthsHaveNoSizeError() {
    for (String username : new String[] {"a".repeat(6), "a".repeat(32)}) {
      User user = validUser();
      user.setUsername(username);
      when(userService.findByUsername(username)).thenReturn(null);
      Errors errors = errorsFor(user);

      validator.validate(user, errors);

      assertThat(errors.getFieldErrors("username"))
        .extracting(error -> error.getCode())
        .doesNotContain("Size.userForm.username");
    }
  }

  @Test
  void blankUsernameHasNotEmptyAndSizeErrors() {
    User user = validUser();
    user.setUsername("   ");
    when(userService.findByUsername("   ")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldErrors("username"))
      .extracting(error -> error.getCode())
      .contains("NotEmpty", "Size.userForm.username");
  }

  @Test
  void duplicateUsernameHasDuplicateError() {
    User user = validUser();
    when(userService.findByUsername("alice1")).thenReturn(new User());
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldError("username").getCode())
      .isEqualTo("Duplicate.userForm.username");
  }

  @Test
  void passwordSevenCharactersHasSizeError() {
    User user = validUser();
    user.setPassword("1234567");
    user.setPasswordConfirm("1234567");
    when(userService.findByUsername("alice1")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldError("password").getCode()).isEqualTo("Size.userForm.password");
  }

  @Test
  void passwordThirtyThreeCharactersHasSizeError() {
    User user = validUser();
    user.setPassword("a".repeat(33));
    user.setPasswordConfirm(user.getPassword());
    when(userService.findByUsername("alice1")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldErrors("password"))
      .extracting(error -> error.getCode())
      .contains("Size.userForm.password");
  }

  @Test
  void passwordBoundaryLengthsHaveNoSizeError() {
    for (String password : new String[] {"a".repeat(8), "a".repeat(32)}) {
      User user = validUser();
      user.setPassword(password);
      user.setPasswordConfirm(password);
      when(userService.findByUsername("alice1")).thenReturn(null);
      Errors errors = errorsFor(user);

      validator.validate(user, errors);

      assertThat(errors.getFieldErrors("password"))
        .extracting(error -> error.getCode())
        .doesNotContain("Size.userForm.password");
    }
  }

  @Test
  void blankPasswordHasNotEmptyButNoSizeError() {
    User user = validUser();
    user.setPassword("        ");
    user.setPasswordConfirm(user.getPassword());
    when(userService.findByUsername("alice1")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldErrors("password"))
      .extracting(error -> error.getCode())
      .contains("NotEmpty")
      .doesNotContain("Size.userForm.password");
  }

  @Test
  void differingPasswordConfirmationHasDiffError() {
    User user = validUser();
    user.setPasswordConfirm("different");
    when(userService.findByUsername("alice1")).thenReturn(null);
    Errors errors = errorsFor(user);

    validator.validate(user, errors);

    assertThat(errors.getFieldError("passwordConfirm").getCode())
      .isEqualTo("Diff.userForm.passwordConfirm");
  }

  @Test
  void nullUsernameThrowsNullPointerException() {
    User user = validUser();
    user.setUsername(null);

    assertThatThrownBy(() -> validator.validate(user, errorsFor(user)))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void nullPasswordThrowsNullPointerException() {
    User user = validUser();
    user.setPassword(null);

    assertThatThrownBy(() -> validator.validate(user, errorsFor(user)))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void nullPasswordConfirmationThrowsNullPointerException() {
    User user = validUser();
    user.setPasswordConfirm(null);

    assertThatThrownBy(() -> validator.validate(user, errorsFor(user)))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void validUserLooksUpUsernameExactlyOnce() {
    User user = validUser();
    when(userService.findByUsername("alice1")).thenReturn(null);

    validator.validate(user, errorsFor(user));

    verify(userService).findByUsername("alice1");
  }

  private static User validUser() {
    User user = new User();
    user.setUsername("alice1");
    user.setPassword("password");
    user.setPasswordConfirm("password");
    return user;
  }

  private static Errors errorsFor(User user) {
    return new BeanPropertyBindingResult(user, "userForm");
  }

  private static class SpecialUser extends User {
  }
}
