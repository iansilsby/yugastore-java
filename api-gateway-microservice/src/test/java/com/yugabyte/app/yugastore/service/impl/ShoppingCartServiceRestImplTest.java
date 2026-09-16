package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.rest.clients.ShoppingCartRestClient;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceRestImplTest {

  @Mock
  ShoppingCartRestClient shoppingCartRestClient;

  @InjectMocks
  ShoppingCartServiceRestImpl service;

  @Test
  void addProductDelegatesAndReturnsClientResult() {
    when(shoppingCartRestClient.addProductToCart("u1001", "B001")).thenReturn("added");

    assertThat(service.addProduct("u1001", "B001")).isEqualTo("added");
    verify(shoppingCartRestClient).addProductToCart("u1001", "B001");
  }

  @Test
  void getProductsInCartDelegatesAndReturnsClientResult() {
    Map<String, Integer> cart = Collections.singletonMap("B001", 2);
    when(shoppingCartRestClient.getProductsInCart("u1001")).thenReturn(cart);

    assertThat(service.getProductsInCart("u1001")).isSameAs(cart);
    verify(shoppingCartRestClient).getProductsInCart("u1001");
  }

  @Test
  void removeProductDelegatesAndReturnsClientResult() {
    when(shoppingCartRestClient.removeProductFromCart("u1001", "B001")).thenReturn("removed");

    assertThat(service.removeProduct("u1001", "B001")).isEqualTo("removed");
    verify(shoppingCartRestClient).removeProductFromCart("u1001", "B001");
  }

  @Test
  void clearCartDelegatesAndReturnsClientResult() {
    when(shoppingCartRestClient.clearCart("u1001")).thenReturn("cleared");

    assertThat(service.clearCart("u1001")).isEqualTo("cleared");
    verify(shoppingCartRestClient).clearCart("u1001");
  }

  @Test
  void clientExceptionPropagatesUnchanged() {
    RuntimeException boom = new RuntimeException("cart down");
    when(shoppingCartRestClient.getProductsInCart("u1001")).thenThrow(boom);

    assertThatThrownBy(() -> service.getProductsInCart("u1001")).isSameAs(boom);
  }
}
