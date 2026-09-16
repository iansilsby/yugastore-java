package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.rest.clients.CheckoutRestClient;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceRestImplTest {

  @Mock
  CheckoutRestClient checkoutRestClient;

  @InjectMocks
  CheckoutServiceRestImpl service;

  @Test
  void checkoutDelegatesAndReturnsClientResult() {
    CheckoutStatus status = new CheckoutStatus();
    when(checkoutRestClient.checkout()).thenReturn(status);

    assertThat(service.checkout()).isSameAs(status);
    verify(checkoutRestClient).checkout();
  }

  @Test
  void checkoutPassesNullThrough() {
    when(checkoutRestClient.checkout()).thenReturn(null);

    assertThat(service.checkout()).isNull();
  }

  @Test
  void clientExceptionPropagatesUnchanged() {
    RuntimeException boom = new RuntimeException("checkout down");
    when(checkoutRestClient.checkout()).thenThrow(boom);

    assertThatThrownBy(() -> service.checkout()).isSameAs(boom);
  }
}
