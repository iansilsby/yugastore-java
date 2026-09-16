package com.yugabyte.app.yugastore.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.service.SecurityService;
import com.yugabyte.app.yugastore.service.UserService;
import com.yugabyte.app.yugastore.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private UserService userService;

  @Mock
  private SecurityService securityService;

  @Mock
  private UserValidator userValidator;

  @Mock
  private Model model;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private UserController controller;

  @BeforeEach
  void setRedirectUrl() {
    ReflectionTestUtils.setField(controller, "redirectURL", "http://example.com/ui");
  }

  @Test
  void getRegistrationAddsUserFormAndReturnsRegistration() {
    assertThat(controller.registration(model)).isEqualTo("registration");

    verify(model).addAttribute(eq("userForm"), any(User.class));
  }

  @Test
  void postRegistrationWithErrorsReturnsRegistrationWithoutSaving() {
    User user = new User();
    when(bindingResult.hasErrors()).thenReturn(true);

    assertThat(controller.registration(user, bindingResult)).isEqualTo("registration");

    verify(userValidator).validate(user, bindingResult);
    verify(userService, never()).save(any(User.class));
  }

  @Test
  void postRegistrationSavesAndRedirectsWithoutAutoLogin() {
    User user = new User();
    when(bindingResult.hasErrors()).thenReturn(false);

    assertThat(controller.registration(user, bindingResult)).isEqualTo("redirect:/login");

    verify(userValidator).validate(user, bindingResult);
    verify(userService).save(user);
    verifyNoInteractions(securityService);
  }

  @Test
  void loginWithoutParametersDoesNotAddAttributes() {
    assertThat(controller.login(model, null, null)).isEqualTo("login");

    verifyNoInteractions(model);
  }

  @Test
  void loginWithErrorAddsInvalidCredentialsMessage() {
    controller.login(model, "", null);

    verify(model).addAttribute("error", "Your username and password is invalid.");
  }

  @Test
  void loginWithLogoutAddsLoggedOutMessage() {
    controller.login(model, null, "true");

    verify(model).addAttribute("message", "You have been logged out successfully.");
  }

  @Test
  void loginWithBothParametersAddsBothMessages() {
    controller.login(model, "error", "true");

    verify(model).addAttribute("error", "Your username and password is invalid.");
    verify(model).addAttribute("message", "You have been logged out successfully.");
  }

  @Test
  void welcomeRedirectsToConfiguredUrlWithoutUsingModel() {
    assertThat(controller.welcome(model)).isEqualTo("redirect:http://example.com/ui");

    verifyNoInteractions(model);
  }
}
