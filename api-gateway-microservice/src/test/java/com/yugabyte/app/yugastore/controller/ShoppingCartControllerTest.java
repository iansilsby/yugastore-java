package com.yugabyte.app.yugastore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.yugabyte.app.yugastore.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.service.CheckoutServiceRest;
import com.yugabyte.app.yugastore.service.ShoppingCartServiceRest;

@ExtendWith(MockitoExtension.class)
class ShoppingCartControllerTest {

  @Mock
  ShoppingCartServiceRest shoppingCartServiceRest;

  @Mock
  CheckoutServiceRest checkoutServiceRest;

  @InjectMocks
  ShoppingCartController controller;

  @Test
  void shoppingCartUsesHardcodedUserId() {
    Map<String, Integer> cart = new HashMap<>();
    cart.put("B001", 2);
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(cart);

    ResponseEntity<Map<String, Integer>> response = controller.shoppingCart();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(cart);
    verify(shoppingCartServiceRest).getProductsInCart("u1001");
  }

  @Test
  void shoppingCartReturns500WhenServiceReturnsNull() {
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(null);

    ResponseEntity<Map<String, Integer>> response = controller.shoppingCart();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void addProductToCartAddsThenFetchesCartInOrder() {
    Map<String, Integer> cart = Collections.singletonMap("B001", 1);
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(cart);

    ResponseEntity<?> response = controller.addProductToCart("B001");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(cart);
    InOrder inOrder = inOrder(shoppingCartServiceRest);
    inOrder.verify(shoppingCartServiceRest).addProduct("u1001", "B001");
    inOrder.verify(shoppingCartServiceRest).getProductsInCart("u1001");
  }

  @Test
  void addProductToCartReturns500WhenCartIsNull() {
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(null);

    ResponseEntity<?> response = controller.addProductToCart("B001");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void addProductToCartIgnoresAddProductReturnValue() {
    // Quirk: whatever addProduct returns is discarded; the body is always the cart map.
    when(shoppingCartServiceRest.addProduct("u1001", "B001")).thenReturn("ignored");
    Map<String, Integer> cart = Collections.singletonMap("B001", 3);
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(cart);

    ResponseEntity<?> response = controller.addProductToCart("B001");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(cart);
  }

  @Test
  void removeProductFromCartRemovesThenFetchesCartInOrder() {
    Map<String, Integer> cart = Collections.emptyMap();
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(cart);

    ResponseEntity<Map<String, Integer>> response = controller.removeProductFromCart("B001");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(cart);
    InOrder inOrder = inOrder(shoppingCartServiceRest);
    inOrder.verify(shoppingCartServiceRest).removeProduct("u1001", "B001");
    inOrder.verify(shoppingCartServiceRest).getProductsInCart("u1001");
  }

  @Test
  void removeProductFromCartReturns500WhenCartIsNull() {
    when(shoppingCartServiceRest.getProductsInCart("u1001")).thenReturn(null);

    ResponseEntity<Map<String, Integer>> response = controller.removeProductFromCart("B001");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void checkoutReturnsOkWithStatus() {
    CheckoutStatus status = new CheckoutStatus();
    status.setStatus(CheckoutStatus.SUCCESS);
    status.setOrderNumber("ORDER-1");
    when(checkoutServiceRest.checkout()).thenReturn(status);

    ResponseEntity<CheckoutStatus> response = controller.checkout();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(status);
  }

  @Test
  void checkoutReturnsOkEvenWhenStatusIsFailure() {
    // Quirk: a failed checkout is still reported as HTTP 200.
    CheckoutStatus status = new CheckoutStatus();
    status.setStatus(CheckoutStatus.FAILURE);
    when(checkoutServiceRest.checkout()).thenReturn(status);

    ResponseEntity<CheckoutStatus> response = controller.checkout();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(status);
    assertThat(response.getBody().getStatus()).isEqualTo(CheckoutStatus.FAILURE);
  }

  @Test
  void checkoutReturnsOkWithNullBodyWhenServiceReturnsNull() {
    when(checkoutServiceRest.checkout()).thenReturn(null);

    ResponseEntity<CheckoutStatus> response = controller.checkout();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();
  }
}
